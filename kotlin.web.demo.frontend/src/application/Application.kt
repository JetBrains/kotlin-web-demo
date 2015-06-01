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

import jquery.jq
import model.ProjectType
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import projectProvider
import providers.*
import statusBarView
import utils.*
import views.AccordionView
import views.buttons.Button
import views.dialogs.ConverterView
import views.dialogs.Dialog
import views.dialogs.ShortcutsDialogView
import views.navBarView
import views.tabs
import kotlin.browser.document
import kotlin.browser.window

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

    private val runProvider = RunProvider(
            onSuccess = { output, project ->
                output.forEach { data ->
                    if (data.type == "errors") {
                        project.setErrors(data.errors);
                        problemsView.addMessages();
                        editor.setHighlighting();
                    } else if (data.type == "toggle-info" || data.type == "info" || data.type == "generatedJSCode") {
                        generatedCodeView.setOutput(data);
                    } else {
                        if (configurationManager.getConfiguration().type == Configuration.type.JUNIT) {
                            junitView.setOutput(data);
                        } else {
                            consoleView.setOutput(data);
                        }
                    }
                }
                statusBarView.setStatus(ActionStatusMessages.run_java_ok);
            },
            onErrorsFound = { data, project ->
                data.forEach { data ->
                    if (data.type == "errors") {
                        project.setErrors(data.errors);
                        jq("#result-tabs").tabs("option", "active", 0);
                        problemsView.addMessages();
                        editor.setHighlighting();
                        statusBarView.setStatus(ActionStatusMessages.get_highlighting_ok,
                                arrayOf(getNumberOfErrorsAndWarnings(data.errors)));
                    }
                }
            },
            onComplete = {
                runButton.disabled = false
            },
            onFail = { error ->
                consoleView.writeException(error);
                statusBarView.setStatus(ActionStatusMessages.run_java_fail);
            }
    )
    val runButtonElement = document.getElementById("runButton") as HTMLElement
    private val runButton = Button(runButtonElement)

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

    val fileProvider = FileProvider(
            { error, status ->
                consoleView.writeException(error);
                statusBarView.setStatus(status);
            },
            {
                editor.reloadFile();
            }
    )

    val headersProvider = HeadersProvider(
            onFail = { message, status ->
                statusBarView.setStatus(status);
                console.log(message);
            },
            onHeadersLoaded = {
                statusBarView.setStatus(ActionStatusMessages.load_headers_ok);
            },
            onProjectHeaderLoaded = {
                statusBarView.setStatus(ActionStatusMessages.load_header_ok);
            },
            onProjectHeaderNotFound = {
                statusBarView.setStatus(ActionStatusMessages.load_header_fail);
                window.alert("Can't find project, maybe it was removed by the user.");
                clearState();
                accordion.loadFirstItem();
            }
    );

    val projectProvider = ProjectProvider(
            onProjectLoaded = {
                statusBarView.setStatus(ActionStatusMessages.load_project_ok)
            },
            onNewProjectAdded = { name, projectId, fileId ->
                accordion.addNewProject(name, projectId, fileId);
            },
            onFail = { message, status ->
                statusBarView.setStatus(status)
                console.log(message)
            }
    )

    val completionProvider = CompletionProvider(
            onSuccess = {
                statusBarView.setStatus(ActionStatusMessages.get_completion_ok);
            },
            onFail = { error ->
                consoleView.writeException(error);
                statusBarView.setStatus(ActionStatusMessages.get_completion_fail);
            }
    );

    val highlightingProvider = HighlightingProvider(
            { data ->
                accordion.selectedProjectView!!.project.setErrors(data);
                problemsView.addMessages(data);
                statusBarView.setStatus(ActionStatusMessages.get_highlighting_ok, arrayOf(getNumberOfErrorsAndWarnings(data)));
            },
            { error, status ->
                unBlockContent();
                consoleView.writeException(error);
                statusBarView.setStatus(ActionStatusMessages.get_highlighting_fail);
            }
    )

    private val saveButton = document.getElementById("saveButton") as HTMLElement

    init {
        initButtons()

        document.onkeydown = { e ->
            var shortcut = actionManager.getShortcut("org.jetbrains.web.demo.run");
            if (shortcut.isPressed(e as KeyboardEvent)) {
                runButtonElement.click();
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
        runButtonElement.onclick = {
            runButton.disabled = true
            consoleView.clear();
            junitView.clear();
            generatedCodeView.clear();
            runProvider.run(configurationManager.getConfiguration(), accordion.selectedProjectView!!.project);
        };
        runButtonElement.title = runButtonElement.title.replace("@shortcut@", actionManager.getShortcut("org.jetbrains.web.demo.run").name)

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

native
fun getNumberOfErrorsAndWarnings(data: dynamic): Int

native fun clearState()