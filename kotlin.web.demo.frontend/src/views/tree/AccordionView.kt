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
import model.UserProject
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
        onProjectSelected: (Project) -> Unit,
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

    private val onProjectDeleted: (ProjectView) -> Unit = { projectView: ProjectView ->
        projectViews.remove(projectView.project.id)
        if (selectedProjectView === projectView) {
            window.history.replaceState("", "", "/")
            selectedProjectView = null
            selectedFileView = null
            loadFirstItem()
        }
    }
    private val onProjectHeaderClick = { projectView: ProjectView ->
        selectProject(projectView.project.id)
    }
    private val onProjectSelected = { projectView: ProjectView ->
        if (projectView.project.files.isEmpty()) {
            selectedFileView = null
        }
        onProjectSelected(projectView.project)
    }
    private val onProjectCreated: (ProjectView) -> Unit = { projectView: ProjectView -> projectViews.put(projectView.project.id, projectView) }

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
                    myProgramsFolder = MyProgramsFolderView(
                            parentNode = element,
                            content = folder,
                            parent = null,
                            onProjectDeleted = onProjectDeleted,
                            onProjectHeaderClick = onProjectHeaderClick,
                            onProjectSelected = onProjectSelected,
                            onProjectCreated = onProjectCreated
                    );
                } else if (folder.name == "Public links") {
                    publicLinksFolder = FolderView(
                            parentNode = element,
                            content = folder,
                            parent = null,
                            onProjectDeleted = onProjectDeleted,
                            onProjectHeaderClick = onProjectHeaderClick,
                            onProjectSelected = onProjectSelected,
                            onProjectCreated = onProjectCreated
                    )
                } else if (folder.name == "Workshop") {
                    FolderViewWithProgress(
                            parentNode = element,
                            content = folder,
                            parent = null,
                            hasProgressBar = true,
                            onProjectDeleted = onProjectDeleted,
                            onProjectHeaderClick = onProjectHeaderClick,
                            onProjectSelected = onProjectSelected,
                            onProjectCreated = onProjectCreated
                    )
                } else {
                    FolderView(
                            parentNode = element,
                            content = folder,
                            parent = null,
                            onProjectDeleted = onProjectDeleted,
                            onProjectHeaderClick = onProjectHeaderClick,
                            onProjectSelected = onProjectSelected,
                            onProjectCreated = onProjectCreated
                    )
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
        val projectView = myProgramsFolder.createProject(ProjectHeader(
                name,
                publicId,
                ProjectType.USER_PROJECT,
                false
        ))
        projectView.project.setDefaultContent()
        (projectView.project as UserProject).addFileWithMain(name, fileId)
        selectProject(publicId)
    }

    fun addNewProjectWithContent(publicId: String, content: dynamic) {
        val projectView = myProgramsFolder.createProject(ProjectHeader(
                content.name,
                publicId,
                ProjectType.USER_PROJECT,
                false
        ))
        projectView.project.setContent(content)
        selectProject(publicId)
    }

    fun onBeforeUnload() {
        var publicLinks = publicLinksFolder.projects.map {
            json(
                    "name" to it.project.name,
                    "publicId" to it.project.id
            )
        }
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
                if (projectId !in myProgramsFolder.projects.map { it.project.id }) {
                    Application.headersProvider.getProjectHeaderById( projectId, { header ->
                        publicLinksFolder.createProject(header)
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