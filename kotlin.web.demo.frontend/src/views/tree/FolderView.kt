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
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import model.Folder
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.jquery.jq
import kotlin.js.json

open class FolderView(parentNode: HTMLElement,
                      val content: Folder,
                      val parent: FolderView? = null,
                      val onProjectDeleted: (ProjectView) -> Unit,
                      val onProjectHeaderClick: (ProjectView) -> Unit,
                      val onProjectSelected: (ProjectView) -> Unit,
                      val onProjectCreated: (ProjectView) -> Unit
) {
    val depth: Int = if (parent == null) 0 else parent.depth + 1
    val projects = arrayListOf<ProjectView>()
    val childFolders = arrayListOf<FolderView>()

    val headerElement = parentNode.append.div {
        classes = setOf("folder-header")
        attributes.put("depth", depth.toString())
        id = content.id
    }

    val container = headerElement.append.div {
        classes = setOf("container")
    }

    protected val folderNameElement: HTMLDivElement = container.append.div {
        +content.name
        classes = setOf("text")
    }

    val contentElement = parentNode.append.div {}

    init {
        content.projects.mapTo(projects) { createProject(it) }
        initializeChildFolders()
        if (!childFolders.isEmpty()) {
            jq(contentElement).accordion(json(
                    "heightStyle" to "content",
                    "collapsible" to true,
                    "navigation" to true,
                    "active" to 0,
                    "icons" to json(
                            "activeHeader" to "examples-open-folder-icon",
                            "header" to "examples-closed-folder-icon"
                    )
            ))
        }
    }

    protected open fun initializeChildFolders(): List<FolderView> = content.childFolders.mapTo(childFolders, {
        FolderView(contentElement, it, this, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated)
    })

    open fun createProject(header: ProjectHeader): ProjectView {
        val projectView = ProjectView(
                header,
                this,
                onProjectHeaderClick,
                onProjectSelected
        )
        onProjectCreated(projectView)
        return projectView
    }

    fun deleteProject(projectView: ProjectView) {
        projects.remove(projectView)
        contentElement.removeChild(projectView.headerElement)
        contentElement.removeChild(projectView.contentElement)
        onProjectDeleted(projectView)
    }

    fun selectFolder(folder: FolderView) {
        jq(contentElement).accordion("option", "active", childFolders.indexOf(folder))
    }

    fun select() {
        if (parent != null) {
            parent.selectFolder(this)
            parent.select()
        } else {
            Application.accordion.selectFolder(this)
        }
    }
}


