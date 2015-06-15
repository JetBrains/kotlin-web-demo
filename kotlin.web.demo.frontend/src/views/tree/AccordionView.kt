/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.tree

import application.Application
import jquery.jq
import model.File
import model.Project
import model.ProjectType
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.*
import utils.jquery.ui.accordion
import utils.jquery.unbind
import views.*
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.dom.removeClass
import kotlin.properties.Delegates

class AccordionView(
        private val element: HTMLElement,
        private val onProjectSelected: (Project) -> Unit,
        private val onSelectFile: (File?, File) -> Unit,
        val onModifiedSelectedFile: (File) -> Unit,
        private val onSelectedFileDeleted: () -> Unit
) {
    private val DEFAULT_PROJECT_ID = "/Examples/Hello,%20world!/Simplest%20version"
    private val projectViews = hashMapOf<String, ProjectView>()
    public var selectedProjectView: ProjectView? = null
        private set
    public var selectedFileView: FileView? = null
        private set
    private var myProgramsFolder: MyProgramsFolderView by Delegates.notNull()
    private var publicLinksFolder: FolderView by Delegates.notNull()

    init {
        element.innerHTML = ""
        jq(element).accordion(json(
                "heightStyle" to "content",
                "navigation" to true,
                "active" to 0,
                "icons" to json(
                        "activeHeader" to "examples-open-folder-icon",
                        "header" to "examples-closed-folder-icon"
                )
        ))
    }

    fun getProjectViewById(id: String) = projectViews[id]

    fun selectedFileDeleted () {
        selectedFileView = null
        onSelectedFileDeleted()
    }

    fun loadAllContent() {
        element.innerHTML = ""
        projectViews.clear()
        selectedProjectView = null
        selectedFileView = null
        Application.headersProvider.getAllHeaders { folders ->
            folders.forEach { folder ->
                if (folder.name == "My programs") {
                    myProgramsFolder = MyProgramsFolderView(element, folder, null, { folderContentElement, header, parent ->
                        addProject(folderContentElement, header, parent)
                    })
                } else if (folder.name == "Public links") {
                    publicLinksFolder = FolderView(element, folder, null, { folderContentElement, header, parent ->
                        addProject(folderContentElement, header, parent)
                    })
                } else {
                    FolderView(element, folder, null, { folderContentElement, header, parent ->
                        addProject(folderContentElement, header, parent)
                    })
                }
            }
            IncompleteActionManager.checkTimepoint("headersLoaded")
            jq(element).accordion("refresh")
            if (!Application.loginView.isLoggedIn) {
                jq(myProgramsFolder.headerElement).unbind("click")
            }
            loadFirstItem()
        }
    }

    fun addNewProject(name: String, publicId: String, fileId: String) {
        myProgramsFolder.addNewProject(addProject(myProgramsFolder.contentElement, ProjectHeader(
                name,
                publicId,
                ProjectType.USER_PROJECT
        ), myProgramsFolder))
        projectViews[publicId]!!.project.setDefaultContent()
        projectViews[publicId]!!.project.addFileWithMain(name, fileId)
        selectProject(publicId)
    }

    fun addNewProjectWithContent(publicId: String, content: dynamic) {
        myProgramsFolder.addNewProject(addProject(myProgramsFolder.contentElement, ProjectHeader(
                content.name,
                publicId,
                ProjectType.USER_PROJECT
        ), myProgramsFolder))
        projectViews[publicId]!!.project.setContent(content)
        selectProject(publicId)
    }

    fun onBeforeUnload() {
        var publicLinks = publicLinksFolder.projects.map { it.header }
        localStorage.setItem("publicLinks", JSON.stringify(publicLinks.toTypedArray()))
    }

    fun validateNewProjectName(name: String) = myProgramsFolder.validateNewProjectName(name)

    fun selectFile(fileView: FileView) {
        if (selectedFileView !== fileView) {
            if (selectedProjectView == fileView.projectView) {
                var previousFileView = selectedFileView
                selectedFileView = fileView

                var previousFile: File? = null
                if (previousFileView != null) {
                    jq(previousFileView.wrapper).removeClass("selected")
                    jq(previousFileView.headerElement).removeClass("selected")
                    previousFile = previousFileView.file
                }
                jq(fileView.wrapper).addClass("selected")
                jq(fileView.headerElement).addClass("selected")

                onSelectFile(previousFile, fileView.file)
            } else {
                throw Exception("You can't select file from project, that isn't selected")
            }
        }
    }

    fun loadFirstItem() {
        var projectId = getProjectIdFromUrl()
        if (projectId == null || projectId == "") {
            if (localStorage.getItem("openedItemId") != null) {
                projectId = localStorage.getItem("openedItemId")!!
            } else {
                projectId = DEFAULT_PROJECT_ID
            }
        }
        localStorage.removeItem("openedItemId")

        if (isUserProjectInUrl()) {
            if (localStorage.getItem(projectId) == null) {
                if (projectId !in projectViews.keySet()) {
                    Application.headersProvider.getProjectHeaderById( projectId, { header ->
                        addProject(publicLinksFolder.contentElement, header, publicLinksFolder)
                        selectProject(projectId)
                    })
                } else {
                    selectProject(projectId)
                }
            } else {
                selectProject(projectId)
            }
        } else if (projectId != "") {
            selectProject(projectId)
        }
    }


    private fun addProject(folderContentElement: HTMLElement, header: ProjectHeader, parent: FolderView): ProjectView {
        //        if (header.type == ProjectType.PUBLIC_LINK && projects[header.publicId] != null) {
        //            return
        //        } else if (projects[header.publicId] != null) {
        //            throw("Duplicate project id")
        //        }
        var projectHeaderElement = document.createElement("div") as HTMLDivElement
        var projectContentElement = document.createElement("div") as HTMLDivElement

        folderContentElement.appendChild(projectHeaderElement)
        folderContentElement.appendChild(projectContentElement)


        var projectView = ProjectView(
                header,
                projectContentElement,
                projectHeaderElement,
                parent,
                onDelete = {
                    if (selectedProjectView === projectViews[header.publicId]) {
                        window.history.replaceState("", "", "index.html")
                        selectedProjectView = null
                        selectedFileView = null
                        loadFirstItem()
                    }
                    projectViews.remove(header.publicId)
                },
                onHeaderClick = { publicId ->
                    selectProject(publicId)
                },
                onSelected = { projectView ->
                    if (projectView.project.files.isEmpty()) {
                        selectedFileView = null
                    }
                    onProjectSelected(projectView.project)
                }
        )
        projectViews.set(header.publicId, projectView)

        return projectView
    }

    fun selectProject(publicId: String) {
        if (selectedProjectView == null || selectedProjectView!!.project.id != publicId) {
            if (selectedProjectView != null) {
                selectedProjectView!!.headerElement.removeClass("selected")
                jq(selectedProjectView!!.contentElement).slideUp()
            }
            selectedProjectView = projectViews[publicId]
            selectedProjectView!!.select()
        }
    }

}