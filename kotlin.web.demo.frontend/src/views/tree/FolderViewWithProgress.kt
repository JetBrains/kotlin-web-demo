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
import model.Project
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import views.tabs.TestResult
import java.util.*
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.dom.addClass


class FolderViewWithProgress(parentNode: HTMLElement,
                             content: dynamic,
                             parent: FolderView?,
                             hasProgressBar: Boolean = false,
                             onProjectDeleted: (ProjectView) -> Unit,
                             onProjectHeaderClick: (ProjectView) -> Unit,
                             onProjectSelected: (ProjectView) -> Unit,
                             onProjectCreated: (ProjectView) -> Unit) :
        FolderView(parentNode, content, parent, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated) {
    val id: String = content.id
    val progressBar = if (hasProgressBar) document.createElement("div") as HTMLDivElement else null
    val counter = document.createElement("div") as HTMLDivElement

    var completedProjects: Set<String>
        get() {
            val storedArray = JSON.parse<Array<String>>(localStorage.get(id) ?: "[]")
            for (name in storedArray)
                Application.accordion.getProjectViewById(name)!!.headerElement.addClass("completed")
            return storedArray.toSet()
        }
        set(value) {
            localStorage.set(id, JSON.stringify(value.toTypedArray()))
            updateProgress()
            if (parent is FolderViewWithProgress)
                parent.updateProgress()
        }

    init {
        contentElement.addClass("progress-folder-content")
        if (progressBar != null) {
            progressBar.className = "progressbar"
            headerElement.appendChild(progressBar)
        }
        headerElement.appendChild(counter)
        counter.className = "counter"
        updateProgress()
    }

    public fun processRunResult(project: Project, completed: Boolean) {
        val publicId = project.id
        val projectView = projects.first { it.project == project }
        if (completed) {
            completedProjects = HashSet(completedProjects + (publicId))
            projectView.headerElement.addClass("completed")
            dialogCloseFun?.invoke()
            openDialog(projectView)
        }
    }

    private var dialogCloseFun: (() -> Unit)? = null;
    private fun openDialog(currentProjectView: ProjectView) {
        val dialogElement = document.createElement("div") as HTMLDivElement
        dialogElement.className = "next-task-dialog"

        val icon = document.createElement("div") as HTMLDivElement
        icon.className = "ok icon"
        dialogElement.appendChild(icon)

        val text = document.createElement("div") as HTMLDivElement
        text.className = "text"
        text.textContent = "Completed!"
        dialogElement.appendChild(text)

        val button = document.createElement("button") as HTMLDivElement
        button.className = "next-task button"
        button.innerHTML = "Start next task"
        dialogElement.appendChild(button)
        button.onclick = {
            Application.accordion.selectProject(getNextProject(currentProjectView).id)
        }

        val close = document.createElement("div") as HTMLDivElement
        close.className = "close icon button"
        dialogElement.appendChild(close)

        dialogCloseFun = Application.editor.openDialog(
                dialogElement,
                {},
                object {
                    val bottom = true
                    val closeOnBlur = false
                    fun onKeyDown(): Boolean {
                        return true
                    }
                }
        )
        close.onclick = { Event -> dialogCloseFun?.invoke() }
    }

    private fun getNextProject(projectView: ProjectView): Project {
        val ind = projects.indexOf(projectView)
        return projects[ind + 1].project
    }

    override protected fun initializeChildFolders(): List<FolderView> = content.childFolders.mapTo(childFolders) {
        FolderViewWithProgress(contentElement, it, this, false, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated)
    }

    private fun getNumberOfCompletedProjects(): Int {
        return childFolders.fold(completedProjects.size(), { numberOfCompletedProjects, folder ->
            if (folder is FolderViewWithProgress)
                numberOfCompletedProjects + folder.getNumberOfCompletedProjects()
            else
                numberOfCompletedProjects
        })
    }


    private fun getNumberOfProjects(): Int {
        return childFolders.fold(projects.size(), { numberOfProjects, folder ->
            if (folder is FolderViewWithProgress)
                numberOfProjects + folder.getNumberOfProjects()
            else
                numberOfProjects
        })
    }

    private fun updateProgress() {
        val numberOfCompletedProjects = getNumberOfCompletedProjects()
        val totalNumberOfProjects = getNumberOfProjects()
        counter.textContent = getNumberOfCompletedProjects().toString() + "/" + getNumberOfProjects()
        if (progressBar != null) {
            val width = (numberOfCompletedProjects.toFloat() / totalNumberOfProjects * 100.0).toString() + "%"
            progressBar.style.setProperty("width", width, "")
        }
    }

}