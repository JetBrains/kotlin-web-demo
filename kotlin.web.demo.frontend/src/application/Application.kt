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

import application.elements.Elements
import model.File
import model.ProjectType
import model.Task
import model.UserProject
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.KeyboardEvent
import providers.*
import utils.*
import utils.jquery.jq
import utils.jquery.on
import utils.jquery.trigger
import utils.jquery.ui.tabs
import views.*
import views.dialogs.ConverterView
import views.dialogs.IframeDialog
import views.dialogs.InputDialogView
import views.dialogs.ShortcutsDialogView
import views.editor.Diagnostic
import views.editor.Editor
import views.tabs.ConsoleView
import views.tabs.GeneratedCodeView
import views.tabs.JUnitView
import views.tabs.ProblemsView
import views.tree.AccordionView
import java.util.*
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window

object Application {
    val actionManager = ActionManager(
            hashMapOf(
                    "org.jetbrains.web.demo.run" to Shortcut(arrayOf("Ctrl", "F9"), { event ->
                        event.keyCode == KeyCode.F9.code && event.ctrlKey
                    }),
                    "org.jetbrains.web.demo.reformat" to Shortcut(arrayOf("Ctrl", "Alt", "L"), { event -> false }),
                    "org.jetbrains.web.demo.autocomplete" to Shortcut(arrayOf("Ctrl", "Space"), { event -> false }),
                    "org.jetbrains.web.demo.save" to Shortcut(arrayOf("Ctrl", "S"), { event ->
                        event.keyCode == KeyCode.S.code && event.ctrlKey
                    })
            ),
            hashMapOf(
                    "org.jetbrains.web.demo.run" to Shortcut(arrayOf("Ctrl", "R"), { event ->
                        event.keyCode == KeyCode.R.code && event.ctrlKey
                    }),
                    "org.jetbrains.web.demo.reformat" to Shortcut(arrayOf("Cmd", "Alt", "L"), { event -> false }),
                    "org.jetbrains.web.demo.autocomplete" to Shortcut(arrayOf("Ctrl", "Space"), { event -> false }),
                    "org.jetbrains.web.demo.save" to Shortcut(arrayOf("Cmd", "S"), { event ->
                        event.keyCode == KeyCode.S.code && event.metaKey
                    })
            )
    )
    val versionView = KotlinVersionView(
            document.getElementById("webdemo-kotlin-version") as HTMLSelectElement,
            onChange = { newValue ->
                val project = accordion.selectedProjectView!!.project
                project.compilerVersion = newValue
                project.save()
                editor.removeStyles()
                problemsView.clear()
                editor.updateHighlighting()
                Unit
            }
    )

    var completedProjects: Set<String>
        get() {
            val storedArray = JSON.parse<Array<String>>(localStorage["completedProjects"] ?: "[]")
            return storedArray.toSet()
        }
        set(value) {
            localStorage.set("completedProjects", JSON.stringify(value.toTypedArray()))
        }

    val accordion: AccordionView = AccordionView(
            document.getElementById("examples-list") as HTMLDivElement,
            onProjectSelected = { project ->
                if (project.files.isEmpty()) {
                    editor.closeFile()
                    if (getSelectedProjectView().project.id != getProjectIdFromUrl()) {
                        setState(userProjectPrefix + project.id, project.name)
                    }
                    navBarView.onProjectSelected(project)
                }
                if (project is UserProject ||
                        (project is Task && loginView.isLoggedIn)) {
                    saveButton.enable()
                } else {
                    saveButton.disable()
                }
                consoleView.clear()
                junitView.clear()
                generatedCodeView.clear()
                problemsView.clear()
                versionView.setVersion(project.compilerVersion);
                jq("#result-tabs").tabs("option", "active", 0)
                Elements.argumentsInputElement.value = project.args
                configurationManager.updateConfiguration(project.confType)
            },
            onSelectFile = { previousFile, currentFile ->
                if (previousFile != null) {
                    if (previousFile.project.type != ProjectType.USER_PROJECT &&
                            previousFile.project.type != ProjectType.ADVENT_OF_CODE_PROJECT) {
                        previousFile.project.save()
                    } else {
                        Application.fileProvider.saveFile(previousFile)
                    }
                }

                val url =
                        if (currentFile.project.type == ProjectType.EXAMPLE ||
                                currentFile.project.type == ProjectType.TASK) {
                            currentFile.id
                        } else if (currentFile.isModifiable) {
                            userProjectPrefix + getSelectedProjectView().project.id + "/" + currentFile.id
                        } else {
                            userProjectPrefix + getSelectedProjectView().project.id + "/" + currentFile.name
                        }
                setState(url, currentFile.project.name)
                navBarView.onFileSelected(previousFile, currentFile)

                editor.closeFile()
                editor.open(currentFile)
            },
            onModifiedSelectedFile = { file ->
                if (file.isModified &&
                        file.project.type == ProjectType.PUBLIC_LINK &&
                        file.project.revertible) {
                    projectProvider.checkIfProjectExists(
                            file.project.id,
                            onExists = {
                                if (file.isRevertible) {
                                    fileProvider.checkFileExistence(
                                            file.id,
                                            {
                                                file.isRevertible = false
                                            }
                                    )
                                }
                            },
                            onNotExists = {
                                file.project.revertible = false
                            }
                    )
                }
            },
            onSelectedFileDeleted = {
                var project = getSelectedProjectView().project
                navBarView.onSelectedFileDeleted()
                replaceState(userProjectPrefix + project.id, project.name)
                editor.closeFile()
            }
    )

    fun getSelectedProjectView() = accordion.selectedProjectView!!

    public val runProvider = RunProvider(
            beforeRun = {
                runButton.disable()
                consoleView.clear()
                junitView.clear()
                generatedCodeView.clear()
            },
            onSuccess = { output, project ->
                if (!isOnlyWarnings(output.errors)) {
                    jq("#result-tabs").tabs("option", "active", 0)
                } else {
                    jq("#result-tabs").tabs("option", "active", 1)
                }
                problemsView.addMessages(output.errors)
                editor.showDiagnostics(output.errors)

                if (output is JunitExecutionResult) {
                    if (project is Task) {
                        val completed = output.testResults.values.flatten().all { it.status == Status.OK.name }
                        if (completed) {
                            projectProvider.saveSolution(project, completed)
                            project.completed = true
                            completedProjects = (completedProjects + project.id).toSet();
                        }
                    }
                    junitView.setOutput(output.testResults.values.flatten())
                } else if (output is JavaRunResult) {
                    consoleView.showJavaRunResult(output)
                }
                statusBarView.setStatus(ActionStatusMessage.run_java_ok)
            },
            processTranslateToJSResult = { translationResult, project ->
                if (!isOnlyWarnings(translationResult.errors)) {
                    jq("#result-tabs").tabs("option", "active", 0)
                } else {
                    jq("#result-tabs").tabs("option", "active", 1)
                }
                problemsView.addMessages(translationResult.errors)
                editor.showDiagnostics(translationResult.errors)
                if (translationResult.jsCode != null) {
                    generatedCodeView.showGeneratedCode(translationResult.jsCode)
                }
                if (translationResult.exception != null) {
                    consoleView.showJsException(translationResult.exception)
                }
                if (translationResult.output != null) {
                    consoleView.showUnmarkedText(translationResult.output)
                }
                statusBarView.setStatus(ActionStatusMessage.run_js_ok)
            },
            onComplete = {
                runButton.enable()
            },
            onFail = { error ->
                consoleView.writeException(error)
                statusBarView.setStatus(ActionStatusMessage.run_java_fail)
            }
    )

    private fun isOnlyWarnings(errors: Map<File, List<Diagnostic>>): Boolean {
        return errors.values.flatten().none {
            it.severity == "ERROR"
        }
    }

    val runButton: Button = Button(
            Elements.runButton,
            onClick = {
                runProvider.run(configurationManager.getConfiguration(),
                        accordion.selectedProjectView!!.project,
                        accordion.selectedFileView!!.file
                )
            }
    )

    val saveButton: Button = Button(
            Elements.saveButton,
            onClick = {
                val selectedProject = accordion.selectedProjectView!!.project
                if (selectedProject is UserProject) {
                    accordion.selectedProjectView!!.project.save()
                    accordion.selectedFileView?.let { Application.fileProvider.saveFile(it.file) }
                } else if (selectedProject.type == ProjectType.TASK && loginView.isLoggedIn) {
                    projectProvider.saveSolution(selectedProject)
                }
            },
            disabled = true
    )

    private val converterProvider = ConverterProvider()
    private val converterView = ConverterView(converterProvider)

    private val iframeDialogs = HashMap<String, IframeDialog>()
    fun getIframeDialog(kotlinVersion: String): IframeDialog {
        val iframeDialog = iframeDialogs.get(kotlinVersion) ?: throw Throwable("No such kotlin version: $kotlinVersion")
        return iframeDialog
    }

    val fileProvider = FileProvider(
            { error, status ->
                consoleView.writeException(error)
                statusBarView.setStatus(status)
            },
            {
                editor.reloadFile()
            },
            { file ->
                file.text = file.userText
                file.isModified = file.userText != file.text
            }
    )

    val headersProvider = HeadersProvider(
            onFail = { message, status ->
                statusBarView.setStatus(status)
                console.log(message)
            },
            onHeadersLoaded = {
                statusBarView.setStatus(ActionStatusMessage.load_headers_ok)
            },
            onProjectHeaderLoaded = {
                statusBarView.setStatus(ActionStatusMessage.load_header_ok)
            },
            onProjectHeaderNotFound = {
                statusBarView.setStatus(ActionStatusMessage.load_header_fail)
                window.alert("Can't find project, maybe it was removed by the user.")
                clearState()
                accordion.loadFirstItem()
            }
    )

    val projectProvider = ProjectProvider(
            onProjectLoaded = {
                statusBarView.setStatus(ActionStatusMessage.load_project_ok)
            },
            onNewProjectAdded = { name, projectId, fileId ->
                accordion.addNewProject(name, projectId, fileId)
            },
            onAdventOfCodeProjectAdded = { name, projectId, fileId, inputFileId, inputFileContent ->
                accordion.addAdventOfCodeProject(name, projectId, fileId, inputFileId, inputFileContent)
            },
            onFail = { message, status ->
                statusBarView.setStatus(status)
                console.log(message)
            }
    )

    val completionProvider = CompletionProvider(
            onSuccess = {
                statusBarView.setStatus(ActionStatusMessage.get_completion_ok)
            },
            onFail = { error, status ->
                consoleView.writeException(error)
                statusBarView.setStatus(status)
            }
    )

    val highlightingProvider = HighlightingProvider(
            { data ->
                problemsView.addMessages(data)
                statusBarView.setStatus(ActionStatusMessage.get_highlighting_ok,
                        getNumberOfErrorsAndWarnings(data).toString())
            },
            { error, status ->
                unBlockContent()
                consoleView.writeException(error)
                statusBarView.setStatus(ActionStatusMessage.get_highlighting_fail)
            }
    )

    val statusBarView = StatusBarView(document.getElementById("statusBar") as HTMLElement)

    val helpProvider = HelpProvider({ error, status ->
        consoleView.writeException(error)
        statusBarView.setStatus(status)
    })

    val editor: Editor = Editor(helpProvider)

    val loginProvider: LoginProvider = LoginProvider(
            beforeLogout = {
                accordion.selectedFileView?.let { Application.fileProvider.saveFile(it.file) }
                accordion.selectedProjectView!!.project.save()
            },
            onLogout = {
                localStorage["isLoggedIn"] = "false";
                loginView.logout()
                statusBarView.setStatus(ActionStatusMessage.logout_ok)
                accordion.loadAllContent()
            },
            onLogin = { data ->
                if (data.isLoggedIn) {
                    loginView.setUserName(data.userName, data.type)
                    statusBarView.setStatus(ActionStatusMessage.login_ok)
                }
                localStorage["isLoggedIn"] = data.isLoggedIn
                accordion.loadAllContent()
            },
            onFail = { exception, actionCode ->
                consoleView.writeException(exception)
                statusBarView.setStatus(actionCode)
            }
    )
    var loginView = LoginView(loginProvider)

    var navBarView = NavBarView(document.getElementById("grid-nav") as HTMLDivElement)


    private fun setKotlinVersions() {
        ajax(
                url = generateAjaxUrl(REQUEST_TYPE.GET_KOTLIN_VERSIONS),
                type = HTTPRequestType.GET,
                dataType = DataType.JSON,
                timeout = 1000,
                success = { kotlinVersions: Array<KotlinWrapperConfig> ->
                    versionView.init(kotlinVersions)
                    kotlinVersions.forEach { wrapperConfig ->
                        val version = wrapperConfig.version
                        iframeDialogs[version] = IframeDialog(version)
                    }
                }
        )
    }

    val generatedCodeView = GeneratedCodeView(document.getElementById("generated-code") as HTMLElement)


    val consoleView = ConsoleView(document.getElementById("program-output") as HTMLDivElement, jq("#result-tabs"))
    val junitView = JUnitView(document.getElementById("program-output") as HTMLDivElement, jq("#result-tabs"))
    val problemsView = ProblemsView(document.getElementById("problems") as HTMLDivElement) { filename, line, ch ->
        accordion.selectedProjectView!!.getFileViewByName(filename)!!.fireSelectEvent()
        editor.setCursor(line, ch)
        editor.focus()
    }

//    val progress = Progress(Elements.progressBar)

    val configurationManager = ConfigurationManager({ configuration ->
        accordion.selectedProjectView!!.project.confType = configuration.type.name.toLowerCase()
        editor.removeStyles()
        problemsView.clear()
        editor.updateHighlighting()
    })

    private fun getNumberOfErrorsAndWarnings(diagnostics: Map<File, List<Diagnostic>>): Int {
        return diagnostics.values.fold(0, { noOfDiagnostics, diagnostics -> noOfDiagnostics + diagnostics.size })
    }

    fun init() {
        jq("#result-tabs").tabs()
        initButtons()
        setKotlinVersions()

        window.onfocus = {
            editor.focus()
        }

        window.onstorage = { event ->
            if (event is StorageEvent && event.key == "isLoggedIn") {
                val isLoggedIn = parseBoolean(event.newValue);
                if (loginView.isLoggedIn != isLoggedIn) window.location.reload()
            }
        }

        window.onbeforeunload = {
            accordion.onBeforeUnload()
            IncompleteActionManager.onBeforeUnload()
            localStorage.setItem("openedItemId", accordion.selectedProjectView!!.project.id)

            accordion.selectedFileView?.let { Application.fileProvider.saveFile(it.file) }
            accordion.selectedProjectView!!.project.save()

            localStorage.setItem("highlightOnTheFly", Elements.onTheFlyCheckbox.checked.toString())
            null
        }

        document.onkeydown = { e ->
            var shortcut = actionManager.getShortcut("org.jetbrains.web.demo.run")
            if (shortcut.isPressed(e as KeyboardEvent)) {
                Elements.runButton.click()
            } else {
                shortcut = actionManager.getShortcut("org.jetbrains.web.demo.save")
                if (shortcut.isPressed(e)) {
                    if (saveButton.disabled) {
                        Elements.saveAsButton.click()
                    } else {
                        Elements.saveButton.click()
                    }
                }
            }
        }

        window.onpopstate = {
            accordion.loadFirstItem()
            accordion.selectedProjectView!!.selectFileFromUrl()
        }

        IncompleteActionManager.registerAction(
                "save",
                "onHeadersLoaded",
                {
                    localStorage.setItem("contentToSave", JSON.stringify(accordion.selectedProjectView!!.project))
                },
                {
                    var content = JSON.parse<dynamic>(localStorage.getItem("contentToSave")!!)
                    localStorage.removeItem("contentToSave")
                    if (content != null && loginView.isLoggedIn) {
                        InputDialogView(
                                "Save project",
                                "Project name:",
                                "Save",
                                content.name,
                                { name ->
                                    accordion.validateNewProjectName(name)
                                },
                                { userInput ->
                                    projectProvider.forkProject(content, { data ->
                                        accordion.addNewProjectWithContent(data.publicId, JSON.parse(data.content))
                                    }, userInput.value)
                                }
                        )
                    }
                }
        )

        //TODO
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.autocomplete").shortcutKeyNames, "Code completion")
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.run").shortcutKeyNames, "Run program")
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.reformat").shortcutKeyNames, "Reformat selected fragment")
        ShortcutsDialogView.addShortcut(actionManager.getShortcut("org.jetbrains.web.demo.save").shortcutKeyNames, "Save current project")
        editor.highlightOnTheFly = Elements.onTheFlyCheckbox.checked

        jq(document).on("click", ".ui-widget-overlay", {
            jq(".ui-dialog-titlebar-close").trigger("click")
        })
    }

    fun initButtons() {
        val converterButton = document.getElementById("java2kotlin-button") as HTMLElement
        converterButton.onclick = { converterView.open() }
        Elements.runButton.title = Elements.runButton.title.replace("@shortcut@", actionManager.getShortcut("org.jetbrains.web.demo.run").name)
        Elements.saveButton.title = Elements.saveButton.title.replace("@shortcut@", actionManager.getShortcut("org.jetbrains.web.demo.save").name)
    }
}

@native interface KotlinWrapperConfig {
    val version: String
    val latestStable: Boolean
    val obsolete: Boolean
}