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
import kotlinx.html.*
import kotlinx.html.js.*
import kotlinx.html.dom.*
import model.Folder
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.jquery.find
import utils.jquery.jq
import utils.jquery.on
import views.dialogs.AdventOfCodeInput
import views.dialogs.AdventOfCodeInputDialog
import views.dialogs.InputDialogView
import views.dialogs.ValidationResult
import views.tree.FolderView
import views.tree.ProjectView

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
        if(type == "ADVENT_OF_CODE_PROJECT"){
            container.append.a {
                href = "http://adventofcode.com/"
                classes = setOf("advent-of-code-link")
                div {
                    classes = setOf("advent-of-code-icon")
                }
            }
        }

        jq(container).find("advent-of-code-link").on("click", { e -> e.stopPropagation()})

        if (!Application.loginView.isLoggedIn) {
            folderNameElement.style.display = "inline-block"
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
                        if (type == "ADVENT_OF_CODE_PROJECT") {
                            AdventOfCodeInputDialog(
                                    dialogTitle = "Add new \"Advent of Code\" project",
                                    inputLabel = "Project name:",
                                    okButtonCaption = "Add",
                                    defaultValue = "Advent",
                                    validate = { name -> validateNewProjectName(name) },
                                    callback = { userInput ->
                                        userInput as AdventOfCodeInput
                                        Application.projectProvider.addAdventOfCodeProject(userInput.value, userInput.codeInput)
                                    }
                            )
                        } else {
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