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

import html4k.dom.append
import html4k.js.div
import jquery.jq
import model.Folder
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.jquery.ui.accordion
import views.tree.ProjectView
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass

//TODO remove addProject function
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

    protected val folderNameElement: HTMLDivElement = headerElement.append.div {
        + content.name
        classes = setOf("text")
    }

    val contentElement = parentNode.append.div{}

    init {
        for (projectHeader in content.projects) {
            projects.add(createProject(projectHeader))
        }
        initializeChildFolders()
        if (!childFolders.isEmpty()) {
            jq(contentElement).accordion(json(
                    "heightStyle" to "content",
                    "navigation" to true,
                    "active" to 0,
                    "icons" to json (
                            "activeHeader" to "examples-open-folder-icon",
                            "header" to "examples-closed-folder-icon"
                    )
            ))
        }
    }

    protected open fun initializeChildFolders(): List<FolderView> = content.childFolders.mapTo(childFolders, {
        FolderView(contentElement, it, this, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated)
    })

    public open fun createProject(header: ProjectHeader): ProjectView{
        val projectView = ProjectView(
                header,
                contentElement.append.div {},
                contentElement.append.div {},
                this,
                onProjectHeaderClick,
                onProjectSelected
        )
        onProjectCreated(projectView)
        return projectView
    }

    fun deleteProject(projectView: ProjectView){
        projects.remove(projectView)
        contentElement.removeChild(projectView.headerElement)
        contentElement.removeChild(projectView.contentElement)
        onProjectDeleted(projectView)
    }


    fun select() {
        parent?.select()
        headerElement.click()
    }
}


