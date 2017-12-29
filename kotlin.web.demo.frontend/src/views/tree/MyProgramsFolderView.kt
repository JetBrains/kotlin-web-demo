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
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import model.Folder
import org.w3c.dom.HTMLElement
import views.dialogs.InputDialogView
import views.dialogs.ValidationResult

class MyProgramsFolderView(parentNode: HTMLElement,
                           content: Folder,
                           parent: FolderView?,
                           type: String,
                           onProjectDeleted: (ProjectView) -> Unit,
                           onProjectHeaderClick: (ProjectView) -> Unit,
                           onProjectSelected: (ProjectView) -> Unit,
                           onProjectCreated: (ProjectView) -> Unit) :
        FolderView(parentNode, content, parent, onProjectDeleted, onProjectHeaderClick, onProjectSelected, onProjectCreated) {


    init {

        folderNameElement.style.display = "inline-block"
        if (!Application.loginView.isLoggedIn) {
            headerElement.style.color = "rgba(0,0,0,0.5)"
            headerElement.onclick = {
                Application.loginView.openLoginDialog()
            }

            headerElement.append.div {
                +"(log in)"
                id = "login-link"
                classes = setOf("login-link")
            }
        } else {
            headerElement.append.div {
                classes = setOf("icons")
                div {
                    classes = setOf("new-project", "icon")
                    onClickFunction = { event ->
                        event.stopPropagation()
                        InputDialogView(
                                dialogTitle = "Add new project",
                                inputLabel = "Project name:",
                                okButtonCaption = "Add",
                                defaultValue = "Untitled",
                                validate = { name -> validateNewProjectName(name) },
                                callback = { userInput -> Application.projectProvider.addNewProject(userInput.value) }
                        )
                    }
                }
            }
        }
    }

    override fun createProject(header: ProjectHeader): ProjectView {
        val projectView = UserProjectView(
                header,
                this,
                onProjectHeaderClick,
                onProjectSelected
        )
        onProjectCreated(projectView)
        projects.add(projectView)
        return projectView
    }

    fun validateNewProjectName(name: String): ValidationResult {
        if (name == "")
            return ValidationResult(false, "Project name can't be empty")
        if (name.length >= 95)
            return ValidationResult(false, "Project name is too long")
        if (!name.matches("^[a-zA-Z0-9,_\\- ]+$"))
            return ValidationResult(false, "Project name can contain only the following characters:" +
                    "<span style=\"font-family: monospace\"> a-z A-Z 0-9 ' ' ',' '_' '-'</span>")
        for (projectView in projects)
            if (projectView.project.name == name) {
                return ValidationResult(false, "Project with that name already exists")
            }
        return ValidationResult(true)
    }
}