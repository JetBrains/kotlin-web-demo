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
import html4k.div
import html4k.dom.append
import html4k.js.div
import html4k.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import views.dialogs.InputDialogView
import views.dialogs.ValidationResult
import views.tree.FolderView
import views.tree.ProjectView

class MyProgramsFolderView(parentNode: HTMLElement,
                           content: dynamic,
                           parent: FolderView?,
                           addProject: (HTMLDivElement, dynamic, FolderView) -> ProjectView) :
        FolderView(parentNode, content, parent, addProject) {


    init {
        if (!Application.loginView.isLoggedIn) {
            folderNameElement.style.display = "inline-block"
            headerElement.style.color = "rgba(0,0,0,0.5)"
            headerElement.onclick = {
                Application.loginView.openLoginDialog()
            }
            headerElement.append.div{
                + "(please log in)"
                id = "login-link"
                classes = setOf("login-link")
            }
        } else {
            headerElement.append.div {
                classes = setOf("icons")
                div {
                    classes = setOf("new-project", "icon")
                    onClickFunction = {
                        InputDialogView.open(
                                title = "Add new project",
                                inputLabel = "Project name:",
                                okButtonCaption = "Add",
                                defaultValue = "Untitled",
                                validate = { name -> validateNewProjectName(name) },
                                callback = { name -> Application.projectProvider.addNewProject(name) }
                        )
                    }
                }
            }
        }
    }

    fun validateNewProjectName(name: String): ValidationResult {
        if (name == "")
            return ValidationResult(false, "Project name can't be empty")
        if (name.length() >= 95)
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

    fun addNewProject(project: ProjectView){
        projects.add(project)
    }

    //TODO replace with more global function removeChild
    fun removeProject(project: ProjectView){
        projects.remove(project)
    }
}