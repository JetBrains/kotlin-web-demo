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

package application

import fileProvider
import jquery.jq
import model.ProjectType
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import projectProvider
import providers.ConverterProvider
import utils.*
import views.AccordionView
import views.dialogs.ConverterView
import views.dialogs.Dialog
import views.dialogs.ShortcutsDialogView
import views.navBarView
import views.tabs
import kotlin.browser.document

class Application {
    val actionManager = ActionManager(
            hashMapOf(
                    "org.jetbrains.web.demo.run" to Shortcut(arrayOf("Ctrl", "F9"), { event ->
                        event.keyCode == KeyCode.F9.code && event.ctrlKey;
                    }),
                    "org.jetbrains.web.demo.reformat" to Shortcut(arrayOf("Ctrl", "Alt", "L"), { event -> false }),
                    "org.jetbrains.web.demo.autocomplete" to Shortcut(arrayOf("Ctrl", "Space"), { event -> false }),
                    "org.jetbrains.web.demo.save" to Shortcut(arrayOf("Ctrl", "S"), { event ->
                        event.keyCode == KeyCode.S.code && event.ctrlKey
                    })
            ),
            hashMapOf(
                    "org.jetbrains.web.demo.run" to Shortcut(arrayOf("Ctrl", "R"), { event ->
                        event.keyCode == KeyCode.R.code && event.ctrlKey;
                    }),
                    "org.jetbrains.web.demo.reformat" to Shortcut(arrayOf("Cmd", "Alt", "L"), { event -> false }),
                    "org.jetbrains.web.demo.autocomplete" to Shortcut(arrayOf("Ctrl", "Space"), { event -> false }),
                    "org.jetbrains.web.demo.save" to Shortcut(arrayOf("Cmd", "S"), { event ->
                        event.keyCode == KeyCode.S.code && event.metaKey
                    })
            )
    )

    val accordion: AccordionView = AccordionView(
            document.getElementById("examples-list") as HTMLDivElement,
            onProjectSelected = { project ->
                if (project.files.isEmpty()) {
                    editor.closeFile();
                    if (accordion.selectedProjectView!!.project.publicId != getProjectIdFromUrl()) {
                        setState(userProjectPrefix + project.publicId, project.name);
                    }
                    navBarView.onProjectSelected(project);
                }
                consoleView.clear();
                junitView.clear();
                generatedCodeView.clear();
                problemsView.addMessages();
                jq("#result-tabs").tabs("option", "active", 0);
                argumentsInputElement.value = project.args;
                configurationManager.updateConfiguration(project.confType);
            },
            onSelectFile = { previousFile, currentFile ->
                if (previousFile != null) {
                    if (previousFile.project.type != ProjectType.USER_PROJECT) {
                        previousFile.project.save();
                    } else {
                        previousFile.save();
                    }
                }

                var url =
                        if (currentFile.project.type == ProjectType.EXAMPLE) {
                            currentFile.id;
                        } else if (currentFile.isModifiable) {
                            userProjectPrefix + accordion.selectedProjectView!!.project.publicId + "/" + currentFile.id;
                        } else {
                            userProjectPrefix + accordion.selectedProjectView!!.project.publicId + "/" + currentFile.name;
                        }
                setState(url, currentFile.project.name);
                navBarView.onFileSelected(previousFile, currentFile);

                editor.closeFile();
                editor.open(currentFile);
            },
            onModifiedSelectedFile = { file ->
                if (file.isModified &&
                        file.project.type == ProjectType.PUBLIC_LINK &&
                        file.project.revertible) {
                    projectProvider.checkIfProjectExists(
                            file.project.publicId,
                            onExists = {
                                if (file.isRevertible) {
                                    fileProvider.checkFileExistence(
                                            file.id,
                                            {
                                                file.isRevertible = false;
                                            }
                                    )
                                }
                            },
                            onNotExists = {
                                file.project.revertible = false;
                            }
                    );
                }
            },
            onSelectedFileDeleted = {
                var project = accordion.selectedProjectView!!.project;
                navBarView.onSelectedFileDeleted();
                setState(userProjectPrefix + project.publicId, project.name);
                editor.closeFile();
            }
    )

    private val converterProvider = ConverterProvider()
    private val converterView = ConverterView(converterProvider)

    public val iframe: HTMLIFrameElement = document.getElementById("k2js-iframe") as HTMLIFrameElement;
    public val iframeDialog: Dialog = Dialog(
            document.getElementById("iframePopup") as HTMLElement,
            width = 640,
            height = 360,
            resizable = false,
            autoOpen = false,
            modal = true,
            onClose = {iframe.clear()}
    )

    private val saveButton = document.getElementById("saveButton") as HTMLElement

    init {
        initButtons()

        document.onkeydown = { e ->
            var shortcut = actionManager.getShortcut("org.jetbrains.web.demo.run");
            if (shortcut.isPressed(e as KeyboardEvent)) {
                runButton.click();
            } else {
                shortcut = actionManager.getShortcut("org.jetbrains.web.demo.save");
                if (shortcut.isPressed(e)) {
                    saveButton.click();
                }
            }
        }

        //TODO
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.autocomplete").shortcutKeyNames, "Code completion");
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.run").shortcutKeyNames, "Run program");
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.reformat").shortcutKeyNames, "Reformat selected fragment");
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.save").shortcutKeyNames, "Save current project");
    }

    fun initButtons() {
        val converterButton = document.getElementById("java2kotlin-button") as HTMLElement
        converterButton.onclick = { converterView.open() };
        runButton.title = runButton.title.replace("@shortcut@", actionManager.getShortcut("org.jetbrains.web.demo.run").name)

        saveButton.title = saveButton.title.replace("@shortcut@", actionManager.getShortcut("org.jetbrains.web.demo.save").name)
        saveButton.onclick = {
            if (accordion.selectedProjectView!!.project.type == ProjectType.USER_PROJECT) {
                accordion.selectedProjectView!!.project.save();
                accordion.selectedFileView?.file?.save();
            } else {
                jq("#saveAsButton").click()
            }
        }
    }
}

native
val runButton: HTMLElement = noImpl

native
fun setState(hash: String, title: String)

native
val userProjectPrefix: String

native
val consoleView: dynamic = noImpl

native
val junitView: dynamic = noImpl

native
val generatedCodeView: dynamic = noImpl

native
val problemsView: dynamic = noImpl

native
val configurationManager: dynamic = noImpl

native
val argumentsInputElement: HTMLInputElement = noImpl