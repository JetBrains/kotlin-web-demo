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
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.img
import model.Folder
import model.Project
import model.Task
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.jquery.circleProgress
import utils.jquery.jq
import views.dialogs.TweetDialog
import kotlin.browser.document
import kotlin.dom.addClass


class FolderViewWithProgress(parentNode: HTMLElement,
                             content: Folder,
                             parent: FolderView?,
                             hasProgressBar: Boolean = false,
                             onProjectDeleted: (ProjectView) -> Unit,
                             onProjectHeaderClick: (ProjectView) -> Unit,
                             onProjectSelected: (ProjectView) -> Unit,
                             onProjectCreated: (ProjectView) -> Unit) :
        FolderView(parentNode, content, parent, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated) {
    val id: String = content.id
    val counter = document.createElement("div") as HTMLDivElement
    var progressBar: ProgressBar? = null

    var radialProgressBar = document.create.div("radial-progressbar")

    init {
        contentElement.addClass("progress-folder-content")
        if (hasProgressBar) {
            val element = container.append.div { classes = setOf("progressbar") }
            container.appendChild(counter)
            counter.className = "counter"
            progressBar = ProgressBar(
                    element = element,
                    projectsNumber = getNumberOfProjects(),
                    levels = content.levels,
                    completedProjectsNumber = getNumberOfCompletedProjects(),
                    onLevelCompleted = { level ->
                        TweetDialog.open(level)
                    }
            )
        } else {
            container.appendChild(radialProgressBar)
            container.appendChild(radialProgressBar)
            radialProgressBar.append.img(src = "/static/images/ok.png")
            jq(radialProgressBar).circleProgress(json(
                    "size" to 18,
                    "startAngle" to -Math.PI / 2,
                    "thickness" to 9,
                    "emptyFill" to "rgba(255, 255, 255, .7)",
                    "animation" to false,
                    "fill" to json("color" to "rgba(0, 0, 0, 0)")
            ));
            updateProgress()
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
            val nextTask = getNextTask(currentProjectView)
            if (nextTask != null)
                Application.accordion.selectProject(nextTask.id)
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

    private fun getNextFolder(folderViewWithProgress: FolderViewWithProgress): FolderViewWithProgress? {
        val index = childFolders.indexOf(folderViewWithProgress);
        return childFolders.getOrNull(index + 1) as FolderViewWithProgress?;
    }

    private fun getNextTask(ind: Int): Project? {
        val projectView = projects.getOrNull(ind + 1) as TaskView?
        if (projectView == null) {
            if (parent !is FolderViewWithProgress) return null;
            val nextFolder = parent.getNextFolder(this);
            return nextFolder?.getNextTask(-1);
        } else {
            if ((projectView.project as Task).completed) return getNextTask(projectView);
            return projectView.project;
        }
    }

    private fun getNextTask(projectView: ProjectView): Project? {
        return getNextTask(projects.indexOf(projectView));
    }

    override fun createProject(header: ProjectHeader): ProjectView {
        if (header !is TaskHeader) throw Exception("Wrong header type")
        val projectView = TaskView(
                header,
                this,
                onProjectHeaderClick,
                onProjectSelected,
                { task ->
                    updateProgress()
                    (parent as FolderViewWithProgress).updateProgress()
                    dialogCloseFun?.invoke()
                    openDialog(task)
                },
                { task ->
                    updateProgress()
                    (parent as FolderViewWithProgress).updateProgress()
                    Application.completedProjects = (Application.completedProjects - task.project.id).toSet()
                }
        )
        onProjectCreated(projectView)
        return projectView
    }

    override fun initializeChildFolders() = content.childFolders.mapTo(childFolders, {
        FolderViewWithProgress(contentElement, it, this, false, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated)
    })

    private fun getNumberOfCompletedProjects(): Int {
        return childFolders.fold(projects.filter { (it.project as Task).completed }.size) { numberOfCompletedProjects, folder ->
            if (folder is FolderViewWithProgress)
                numberOfCompletedProjects + folder.getNumberOfCompletedProjects()
            else
                numberOfCompletedProjects
        }
    }


    private fun getNumberOfProjects(): Int {
        return childFolders.fold(projects.size) { numberOfProjects, folder ->
            if (folder is FolderViewWithProgress)
                numberOfProjects + folder.getNumberOfProjects()
            else
                numberOfProjects
        }
    }

    private fun updateProgress() {
        val numberOfCompletedProjects = getNumberOfCompletedProjects()
        val totalNumberOfProjects = getNumberOfProjects()
        counter.textContent = getNumberOfCompletedProjects().toString() + "/" + getNumberOfProjects()
        val value = numberOfCompletedProjects.toFloat() / totalNumberOfProjects
        progressBar?.updateProgress(numberOfCompletedProjects)
        jq(radialProgressBar).circleProgress(json("value" to value));
    }

}

class ProgressBar(
        val element: HTMLElement,
        val projectsNumber: Int,
        val onLevelCompleted: (Int) -> Unit,
        completedProjectsNumber: Int,
        levels: List<Int>) {
    val emptyFill = element.append.div {
        classes = setOf("empty-fill")
    }
    val levelMarks: List<LevelMark> = initializeLevels(levels)

    init {
        updateElements(completedProjectsNumber)
    }

    private fun initializeLevels(levels: List<Int>): List<LevelMark> {
        return levels.mapIndexed { ind: Int, it: Int ->
            val mark = element.append.div {
                classes = setOf("level-mark-container")
                div {
                    classes = setOf("level-mark")
                }
            }
            mark.style.position = "absolute"
            mark.style.left = (it.toFloat() * 100f / projectsNumber).toString() + "%"
            mark.title = it.toString() + "/" + projectsNumber
            LevelMark(ind + 1, it, mark)
        }
    }

    private fun updateElements(completedProjectsNumber: Int){
        val incompletePercent = ( 1f - completedProjectsNumber.toFloat() / projectsNumber) * 100
        emptyFill.style.width = incompletePercent.toString() + "%"
        levelMarks.forEach {
            if (it.neededNumberOfProjects > completedProjectsNumber) it.show() else it.hide()
        }
    }

    fun updateProgress(completedProjectsNumber: Int) {
        updateElements(completedProjectsNumber)
        levelMarks.forEach {
            if (it.neededNumberOfProjects == completedProjectsNumber) onLevelCompleted(it.level)
        }
    }
}

class LevelMark(val level: Int, val neededNumberOfProjects: Int, val element: HTMLElement) {
    fun hide() {
        element.style.display = "none"
    }

    fun show() {
        element.style.display = ""
    }
}