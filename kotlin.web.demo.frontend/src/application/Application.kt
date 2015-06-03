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
import providers.*
import utils.*
import utils.jquery
import utils.jquery.on
import utils.jquery.trigger
import utils.jquery.ui.tabs
import views.*
import utils.jquery.ui.Button
import views.dialogs.ConverterView
import utils.jquery.ui.Dialog
import views.dialogs.InputDialogView
import views.dialogs.ShortcutsDialogView
import views.editor.Editor
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window

object Application {
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
                Elements.argumentsInputElement.value = project.args;
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
                        if (configurationManager.getConfiguration().type == ConfigurationType.JUNIT) {
                            junitView.setOutput(data);
                        } else {
                            consoleView.setOutput(data);
                        }
                    }
                }
                statusBarView.setStatus(ActionStatusMessage.run_java_ok);
            },
            onErrorsFound = { data, project ->
                data.forEach { data ->
                    if (data.type == "errors") {
                        project.setErrors(data.errors);
                        jq("#result-tabs").tabs("option", "active", 0);
                        problemsView.addMessages();
                        editor.setHighlighting();
                        statusBarView.setStatus(ActionStatusMessage.get_highlighting_ok,
                                getNumberOfErrorsAndWarnings(data.errors).toString());
                    }
                }
            },
            onComplete = {
                runButton.disabled = false
            },
            onFail = { error ->
                consoleView.writeException(error);
                statusBarView.setStatus(ActionStatusMessage.run_java_fail);
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
            onClose = { iframe.clear() }
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
                statusBarView.setStatus(ActionStatusMessage.load_headers_ok);
            },
            onProjectHeaderLoaded = {
                statusBarView.setStatus(ActionStatusMessage.load_header_ok);
            },
            onProjectHeaderNotFound = {
                statusBarView.setStatus(ActionStatusMessage.load_header_fail);
                window.alert("Can't find project, maybe it was removed by the user.");
                clearState();
                accordion.loadFirstItem();
            }
    );

    val projectProvider = ProjectProvider(
            onProjectLoaded = {
                statusBarView.setStatus(ActionStatusMessage.load_project_ok)
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
                statusBarView.setStatus(ActionStatusMessage.get_completion_ok);
            },
            onFail = { error, status ->
                consoleView.writeException(error);
                statusBarView.setStatus(status);
            }
    );

    val highlightingProvider = HighlightingProvider(
            { data ->
                accordion.selectedProjectView!!.project.setErrors(data);
                problemsView.addMessages();
                statusBarView.setStatus(ActionStatusMessage.get_highlighting_ok,
                        getNumberOfErrorsAndWarnings(data).toString());
            },
            { error, status ->
                unBlockContent();
                consoleView.writeException(error);
                statusBarView.setStatus(ActionStatusMessage.get_highlighting_fail);
            }
    )

    private val saveButton = document.getElementById("saveButton") as HTMLElement

    val statusBarView = StatusBarView(document.getElementById("statusBar") as HTMLElement)

    val helpProvider = HelpProvider({ error, status ->
        consoleView.writeException(error);
        statusBarView.setStatus(status);
    });
    val helpViewForWords = HelpView(helpProvider);

    var timeoutId: Int = 0
    val editor: Editor = Editor(
            { cursorPosition ->
                helpViewForWords.hide();
                var pos = editor.cursorCoords();
                helpViewForWords.setPosition(pos);

                window.clearTimeout(timeoutId);
                timeoutId = window.setTimeout({
                    helpViewForWords.update(editor.getWordAtCursor(cursorPosition))
                }, 1000);
            }
    );

    val loginProvider: LoginProvider = LoginProvider(
            {
                if (accordion.selectedFileView != null) {
                    accordion.selectedFileView!!.file.save();
                }
                accordion.selectedProjectView!!.project.save();
            },
            {
                getSessionInfo({ data ->
                    sessionId = data.id;
                    loginView.logout();
                    statusBarView.setStatus(ActionStatusMessage.logout_ok);
                    accordion.loadAllContent();
                });
            },
            { data ->
                if (data.isLoggedIn) {
                    loginView.setUserName(data.userName, data.type);
                    statusBarView.setStatus(ActionStatusMessage.login_ok);
                }
                accordion.loadAllContent();
            },
            { exception, actionCode ->
                consoleView.writeException(exception);
                statusBarView.setStatus(actionCode);
            }
    );
    var loginView = LoginView(loginProvider);

    var navBarView = NavBarView(document.getElementById("grid-nav") as HTMLDivElement);


    private fun setKotlinVersion() {
        ajax(
                url = "http://kotlinlang.org/latest_release_version.txt",
                type = HTMLRequestType.GET,
                dataType = DataType.TEXT,
                timeout = 1000,
                success = { kotlinVersion ->
                    (document.getElementById("kotlinlang-kotlin-version") as HTMLElement).innerHTML = "(" + kotlinVersion + ")";
                }
        );
        ajax(
                url = "build.txt",
                type = HTMLRequestType.GET,
                dataType = DataType.TEXT,
                timeout = 1000,
                success = { kotlinVersion ->
                    (document.getElementById("webdemo-kotlin-version") as HTMLElement).innerHTML = kotlinVersion;
                }
        );
    }

    fun getSessionInfo(callback: (dynamic) -> Unit) {
        ajax(
                url = "kotlinServer?sessionId=" + sessionId + "&type=getSessionInfo",
                type = HTMLRequestType.GET,
                dataType = DataType.JSON,
                timeout = 10000,
                success = callback
        );
    }


    val generatedCodeView = GeneratedCodeView(document.getElementById("generated-code") as HTMLElement);


    val consoleView = ConsoleView(document.getElementById("program-output") as HTMLDivElement, jq("#result-tabs"));
    val junitView = JUnitView(document.getElementById("program-output") as HTMLDivElement, jq("#result-tabs"));
    val problemsView = ProblemsView(
            document.getElementById("problems") as HTMLDivElement,
            jq("#result-tabs"),
            { filename, line, ch ->
                accordion.selectedProjectView!!.getFileViewByName(filename)!!.fireSelectEvent();
                editor.setCursor(line, ch);
                editor.focus();
            }
    );

    val configurationManager = ConfigurationManager({ configuration ->
        accordion.selectedProjectView!!.project.confType = configuration.type.name().toLowerCase();
        editor.removeHighlighting();
        problemsView.clear();
        editor.updateHighlighting()
    });

    private fun getNumberOfErrorsAndWarnings(data: dynamic): Int {
        var noOfErrorsAndWarnings = 0;
        for (filename in Object.keys(data)) {
            noOfErrorsAndWarnings += (data[filename].length as Int)
        }
        return noOfErrorsAndWarnings
    }

    fun init() {
        jq("#result-tabs").tabs();
        initButtons()
        setKotlinVersion();
        window.onfocus = {
            getSessionInfo({ data ->
                if (sessionId != data.id || data.isLoggedIn != loginView.isLoggedIn) {
                    window.location.reload();
                }
            })
        };

        window.onbeforeunload = {
            accordion.onBeforeUnload();
            IncompleteActionManager.onBeforeUnload();
            localStorage.setItem("openedItemId", accordion.selectedProjectView!!.project.publicId);

            if (accordion.selectedFileView != null) {
                accordion.selectedFileView!!.file.save();
            }
            accordion.selectedProjectView!!.project.save();

            localStorage.setItem("highlightOnTheFly", Elements.onTheFlyCheckbox.checked.toString());
            null
        };

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

        window.onpopstate = {
            var projectId = getProjectIdFromUrl();
            if (accordion.getProjectViewById(projectId) == null) {
                accordion.loadFirstItem()
            } else {
                if (accordion.selectedProjectView!!.project.publicId != projectId) {
                    accordion.selectProject(projectId);
                }
                accordion.selectedProjectView!!.selectFileFromUrl();
            }
        };

        IncompleteActionManager.registerAction(
                "save",
                "onHeadersLoaded",
                {
                    localStorage.setItem("contentToSave", JSON.stringify(accordion.selectedProjectView!!.project));
                },
                {
                    var content = JSON.parse<dynamic>(localStorage.getItem("contentToSave")!!);
                    localStorage.removeItem("contentToSave");
                    if (content != null && loginView.isLoggedIn) {
                        InputDialogView.open(
                                "Save project",
                                "Project name:",
                                "Save",
                                content.name,
                                { name ->
                                    accordion.validateNewProjectName(name)
                                },
                                { name ->
                                    projectProvider.forkProject(content, { data ->
                                        accordion.addNewProjectWithContent(data.publicId, JSON.parse(data.content));
                                    }, name)
                                }
                        )
                    }
                }
        );

        //TODO
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.autocomplete").shortcutKeyNames, "Code completion");
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.run").shortcutKeyNames, "Run program");
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.reformat").shortcutKeyNames, "Reformat selected fragment");
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.save").shortcutKeyNames, "Save current project");
        editor.highlightOnTheFly = Elements.onTheFlyCheckbox.checked
        getSessionInfo({ data ->
            sessionId = data.id;
        });


        jquery.jq(document).on("click", ".ui-widget-overlay", {
            jq(".ui-dialog-titlebar-close").trigger("click");
        });
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