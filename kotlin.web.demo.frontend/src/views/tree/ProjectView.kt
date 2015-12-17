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
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import utils.addKotlinExtension
import utils.getFileIdFromUrl
import utils.jquery.slideDown
import views.dialogs.InputDialogView
import views.dialogs.ValidationResult
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass
import kotlinx.html.*
import kotlinx.html.js.*
import kotlinx.html.dom.*
import model.*
import org.w3c.dom.HTMLElement
import utils.jquery.jq

open class ProjectView(
        header: ProjectHeader,
        val parent: FolderView,
        private val onHeaderClick: (ProjectView) -> Unit,
        private val onSelected: (ProjectView) -> Unit
) {
    protected val onFileDeleted: (String) -> Unit = { publicId: String ->
        val fileView = fileViews.get(publicId)!!
        if (fileView.isSelected()) {
            Application.accordion.selectedFileDeleted()
        }
        fileViews.remove(publicId)
        if (!project.files.isEmpty()) {
            fileViews[project.files[0].id]!!.fireSelectEvent()
        }
    }

    protected val onContentLoaded: (List<File>)-> Unit = { files: List<File> ->
        contentElement.innerHTML = ""

        nameSpan.innerHTML = project.name
        nameSpan.title = nameSpan.innerHTML

        for (file in files) {
            fileViews[file.id] = createFileView(file)
        }

        if (!files.isEmpty()) {
            if (Application.accordion.selectedProjectView!!.project === project) {
                selectFileFromUrl()
                onSelected(this)
            }
        } else if (Application.accordion.selectedProjectView!!.project === project) {
            onSelected(this)
            Application.editor.closeFile()
        }
    }

    protected val onContentNotFound: ()-> Unit = {
        if (project.type == ProjectType.PUBLIC_LINK) {
            window.alert("Can't find project origin, maybe it was removed by the user.")
            project.revertible = false
            if (!project.contentLoaded) {
                parent.deleteProject(this)
            }
        }
    }

    val depth = parent.depth + 1
    val project: Project = initProject(header)
    val fileViews = hashMapOf<String, FileView>()
    val headerElement = parent.contentElement.append.div {

        id = header.publicId
        classes = setOf("examples-project-name")
        attributes.set("depth", depth.toString())
        div("icon")
    }
    val contentElement = parent.contentElement.append.div()
    val nameSpan = headerElement.append.span {
        +header.name
        title = header.name
        classes = setOf("file-name-span")
    }

    val actionIconsElement = headerElement.append.div("icons")


    init {
        jq(contentElement).slideUp()
        headerElement.onclick = {
            onHeaderClick(this)
        }

        if (header.type == ProjectType.USER_PROJECT || header.type == ProjectType.PUBLIC_LINK || header.type == ProjectType.ADVENT_OF_CODE_PROJECT) {
            var deleteButton = document.createElement("div") as HTMLDivElement
            deleteButton.className = "delete icon"
            deleteButton.title = "Delete this project"
            deleteButton.onclick = { event ->
                if (window.confirm("Delete project " + header.name + "?")) {
                    Application.projectProvider.deleteProject(header.publicId, header.type, {parent.deleteProject(this)})
                }
                event.stopPropagation()
            }
            actionIconsElement.appendChild(deleteButton)
        }

        if (header.type != ProjectType.USER_PROJECT || header.type == ProjectType.ADVENT_OF_CODE_PROJECT) {
            if (header.modified) headerElement.addClass("modified")

            var revertIcon = document.createElement("div") as HTMLDivElement
            revertIcon.className = "revert icon"
            revertIcon.title = "Revert this project"
            actionIconsElement.appendChild(revertIcon)

            revertIcon.onclick = {
                project.loadOriginal()
            }

            project.revertibleListener.addModifyListener { event ->
                if(!event.newValue)
                    revertIcon.parentNode!!.removeChild(revertIcon)
            }
        }
    }

    protected open fun initProject(header: ProjectHeader): Project {
        val project =
                if (header.type == ProjectType.PUBLIC_LINK) {
                    PublicLink(
                            header.publicId,
                            header.name,
                            parent.content,
                            onFileDeleted,
                            onContentLoaded,
                            onContentNotFound
                    )
                } else if (header.type == ProjectType.EXAMPLE) {
                    Example(
                            header.publicId,
                            header.name,
                            parent.content,
                            onContentLoaded,
                            onContentNotFound
                    )
                } else {
                    throw Exception("Bad type")
                }
        project.modifiedListener.addModifyListener { event ->
            if (event.newValue) {
                headerElement.addClass("modified")
            } else {
                headerElement.removeClass("modified")
            }
        }
        return project
    }

    fun select() {
        parent.select()
        headerElement.className += " selected"
        jq(contentElement).slideDown()
        if (!project.contentLoaded) {
            project.loadContent(false)
        } else {
            if (!project.files.isEmpty()) {
                selectFileFromUrl()
            }
            onSelected(this)
        }
    }

    fun selectFileFromUrl() {
        val fileFromUrl = fileViews.get(getFileIdFromUrl())
        if(fileFromUrl != null) {
            fileFromUrl.fireSelectEvent()
        } else{
            fileViews[project.files[0].id]!!.fireSelectEvent()
        }
    }

    protected fun createFileView(file: File): FileView = FileView(this, contentElement, file)

    protected  fun isSelected(): Boolean {
        return Application.accordion.selectedProjectView === this
    }

    fun getFileViewByName(name: String) = fileViews.values().firstOrNull {
        it.file.name == name
    }

}

open
class ProjectHeader(val name: String, val publicId: String, val type: ProjectType, val modified: Boolean)