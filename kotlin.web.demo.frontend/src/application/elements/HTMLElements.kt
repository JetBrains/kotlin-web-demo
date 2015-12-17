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

package application.elements

import application.Application
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import utils.IncompleteActionManager
import utils.jquery.jq
import utils.parseBoolean
import utils.jquery.ui.selectmenu
import views.dialogs.AskDialog
import views.dialogs.InputDialogView
import views.dialogs.ShortcutsDialogView
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.dom.onClick


internal object Elements{
    val argumentsInputElement = document.getElementById("arguments") as HTMLInputElement
    val shortcutsButton = document.getElementById("shortcuts-button") as HTMLElement
    val onTheFlyCheckbox = document.getElementById("on-the-fly-checkbox") as HTMLInputElement
    val saveAsButton = document.getElementById("saveAsButton") as HTMLElement
    val runMode = document.getElementById("runMode") as HTMLElement
    val runButton = document.getElementById("runButton") as HTMLElement
    val saveButton = document.getElementById("saveButton") as HTMLElement

    fun init() {
        argumentsInputElement.oninput = {
            Application.accordion.selectedProjectView!!.project.args = argumentsInputElement.value
            Unit
        }

        shortcutsButton.onclick = {
            ShortcutsDialogView.open()
        }

        onTheFlyCheckbox.checked = parseBoolean(localStorage.getItem("highlightOnTheFly"))
        onTheFlyCheckbox.onchange = {
            Application.editor.highlightOnTheFly = onTheFlyCheckbox.checked
            Application.editor.updateHighlighting()
        }

        jq(runMode).selectmenu(json(
            "icons" to json( "button" to "selectmenu-arrow-icon" )
        ))


        saveAsButton.onclick = {
            if (Application.loginView.isLoggedIn) {
                InputDialogView(
                        "Save project",
                        "Project name:",
                        "Save",
                        Application.accordion.selectedProjectView!!.project.name,
                        { name ->
                            Application.accordion.validateNewProjectName(name)
                        },
                        { userInput ->
                            Application.projectProvider.forkProject(Application.accordion.selectedProjectView!!.project, { data ->
                                Application.accordion.selectedProjectView!!.project.loadOriginal()
                                Application.accordion.addNewProjectWithContent(data.publicId, JSON.parse(data.content))
                            }, userInput.value)
                        }
                )
            } else {
                IncompleteActionManager.incomplete("save")
                Application.loginView.openLoginDialog({
                    IncompleteActionManager.cancel("save")
                })
            }
        }
    }
}