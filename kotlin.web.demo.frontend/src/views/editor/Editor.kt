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

package views.editor

import kotlinx.html.dom.*
import kotlinx.html.js.*
import kotlinx.html.*
import application.Application
import model.File
import model.FileType
import model.Task
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import providers.HelpProvider
import utils.KeyCode
import utils.codemirror.*
import utils.jquery.JQuery
import utils.jquery.find
import utils.jquery.hide
import utils.jquery.jq
import utils.jquery.ui.toggle
import utils.unEscapeString
import kotlin.browser.document
import kotlin.browser.window

public class Editor(
        private val helpProvider: HelpProvider
) {
    val codeMirror = CodeMirror.fromTextArea(document.getElementById("code") as HTMLTextAreaElement, json(
            "lineNumbers" to true,
            "styleActiveLine" to true,
            "matchBrackets" to true,
            "mode" to "text/x-kotlin",
            "autoCloseBrackets" to true,
            "continueComments" to true,
            "hintOptions" to json("async" to true),
            "gutters" to arrayOf("errors-and-warnings-gutter"),
            "indentUnit" to 4
    ))
    var openedFile: File? = null
    private val timerIntervalForNonPrinting = 300
    var highlightOnTheFly = false
    var arrayClasses = arrayListOf<dynamic>()
    private val documents = hashMapOf<File, CodeMirror.Doc>()
    private var storedCompletionsList: List<CompletionView>? = null
    private val dialogCloseFunctions = arrayListOf<dynamic>()
    private var helpWidget: CodeMirror.LineWidget? = null

    init {
        var timeoutId: Int? = null
        codeMirror.on("change", { codeMirror ->
            helpWidget?.let {
                if (it.line.lineNo() != 0) {
                    it.clear()
                    val tmpWidget = codeMirror.getDoc().addLineWidget(0, it.node, json("above" to true, "noHScroll" to true))
                    it.clear()
                    helpWidget = tmpWidget
                }
            }

            removeStyles()
            if (openedFile != null) {
                openedFile!!.userText = codeMirror.getValue()
                if (timeoutId != null) {
                    window.clearTimeout(timeoutId ?: 0)
                    timeoutId = window.setTimeout({ getHighlighting() }, timerIntervalForNonPrinting)
                } else {
                    timeoutId = window.setTimeout({ getHighlighting() }, timerIntervalForNonPrinting)
                }
            }

        })

        codeMirror.on("mousedown", { instance: CodeMirror, event: MouseEvent ->
            val position = instance.coordsChar(Coordinates(event.pageX, event.pageY))

            //Hack to ignore widget clicks
            if (position.line != 0 || position.ch != 0) {
                val markers = instance.findMarksAt(position)

                val todoMarker = markers.firstOrNull { it.className == "taskWindow" }
                if (todoMarker != null) {
                    val range = todoMarker.find()
                    instance.setSelection(range.from, range.to)
                    instance.focus()
                    event.preventDefault()
                }
            }
        })

        codeMirror.on("keydown", { instance: CodeMirror, event: KeyboardEvent ->
            when (event.keyCode) {
                KeyCode.ENTER.code -> {
                    instance.findMarksAt(instance.getCursor()).firstOrNull { it.className == "taskWindow" }?.clear()
                }
            }
        })

        var helpTimeout = 0
        codeMirror.on("cursorActivity", { codemirror ->
            val cursorPosition = codemirror.getCursor()
            HelpViewForWords.hide()
            window.clearTimeout(helpTimeout)

            helpProvider.getHelpForWord(codemirror.getTokenAt(cursorPosition).string)?.let { help ->
                helpTimeout = window.setTimeout({
                    HelpViewForWords.show(help, codemirror.cursorCoords())
                }, 1000)
            }
        })

        codeMirror.on("blur", { cm ->
            window.clearTimeout(helpTimeout)
            HelpViewForWords.hide()
        })

        codeMirror.on("endCompletion") { cm ->
            storedCompletionsList = null
        }

        val helperFunction: dynamic = { cm: dynamic, callback: dynamic, options: dynamic ->
            getCompletions(cm, callback, options)
        }
        helperFunction.async = true
        CodeMirror.registerHelper("hint", "text/x-kotlin", helperFunction)

        if (window.navigator.appVersion.indexOf("Mac") != -1) {
            codeMirror.setOption("extraKeys", json(
                    "Cmd-Alt-L" to "indentAuto",
                    "Ctrl-Space" to { mirror: dynamic ->
                        CodeMirror.commands.autocomplete(mirror, CodeMirror.hint.kotlin, json("async" to true))
                    },
                    "Shift-Tab" to "indentLess",
                    "Ctrl-/" to "toggleComment",
                    "Cmd-[" to false,
                    "Cmd-]" to false
            ))
        } else {
            codeMirror.setOption("extraKeys", json(
                    "Ctrl-Alt-L" to "indentAuto",
                    "Ctrl-Space" to { mirror: dynamic ->
                        CodeMirror.commands.autocomplete(mirror, CodeMirror.hint.kotlin, json("async" to true))
                    },
                    "Shift-Tab" to "indentLess",
                    "Ctrl-/" to "toggleComment",
                    "Ctrl-[" to false,
                    "Ctrl-]" to false
            ))
        }
    }

    fun refresh() {
        codeMirror.refresh()
    }

    fun setCursor(lineNo: Int, charNo: Int) {
        codeMirror.setCursor(lineNo, charNo)
    }

    fun focus() {
        codeMirror.focus()
    }

    fun open(file: File) {
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "none"

        file.project.files.forEach { createDocIfNotExist(it) }
        val relatedDocument = documents.get(file)!!
        if (openedFile == null) {
            openedFile = file
            removeStyles()
            codeMirror.setOption("readOnly", !openedFile!!.isModifiable)
            codeMirror.focus()
            codeMirror.swapDoc(relatedDocument)
            helpWidget = codeMirror.getLineHandle(0).widgets?.getOrNull(0)
            codeMirror.refresh()
            Application.accordion.onModifiedSelectedFile(file)
        } else {
            throw Exception("Previous file wasn't closed")
        }
    }

    fun closeFile() {
        for (closeFunction in dialogCloseFunctions) {
            closeFunction()
        }
        dialogCloseFunctions.clear()
        codeMirror.swapDoc(CodeMirror.Doc(""))
        openedFile = null
        removeStyles()
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "block"
    }

    fun reloadFile() {
        val openedFile = this.openedFile;
        this.openedFile = null
        if (openedFile != null) {
            documents.remove(openedFile);
            open(openedFile)
        };
    }

    fun updateHighlighting() {
        getHighlighting()
    }

    private fun createDocIfNotExist(file: File) {
        if (documents.get(file) == null) {
            val type = if (file.type != FileType.JAVA_FILE.name) {
                "text/x-kotlin"
            } else {
                "text/x-java"
            }
            val cmDocument = CodeMirror.Doc(file.userText, type);
            documents.put(file, cmDocument);
            getHighlighting()


            if (!(file.project is Task && file.name == "Task.kt")) return
            val helpWrapper = document.create.div("task-help-wrapper")
            val help = helpWrapper.append.div { classes = setOf("task-help") }
            val helpContent = JQuery.parseHTML(file.project.help)
            helpContent?.forEach { help.appendChild(it) }
            jq(help).find("a").attr("target", "_blank")

            val buttonSet = help.append.div { classes = setOf("buttonset") }
            buttonSet.append.div {
                classes = setOf("button", "default-button")
                div {
                    classes = setOf("text")
                    + "Check"
                }
                onClickFunction = {
                    Application.runProvider.run(
                            Application.configurationManager.getConfiguration(),
                            Application.accordion.selectedProjectView!!.project,
                            Application.accordion.selectedFileView!!.file
                    )
                }
            }

            buttonSet.append.div {
                classes = setOf("button", "default-button")
                div {
                    classes = setOf("text")
                    + "Revert"
                }
                onClickFunction = {
                    file.project.loadOriginal()
                }
            }

        if (file.solutions != null && file.solutions.isNotEmpty()) {
            val answerButton = buttonSet.append.div {
                classes = setOf("button", "default-button")
            }
            val answerButtonText = answerButton.append.div {
                classes = setOf("text")
                + "Show answer"
            }
            answerButton.style.transform = "rotate(180deg)"
            answerButtonText.style.top = "3px"

            var answerHidden = true
            answerButton.onclick = {
                answerHidden = !answerHidden
                answerButtonText.textContent = "${if (answerHidden) "Show" else "Hide"} answer"
                answerButtonText.style.top = if (answerHidden) "3px" else ""
                answerButton.style.transform = if (answerHidden) "rotate(180deg)" else ""
                jq(".task-answer").toggle()
                helpWidget?.changed()
            }

            file.solutions.forEach {
                val answer = document.create.pre {
                    classes = setOf("task-answer")
                    code {
                        attributes.put("data-lang", "text/x-kotlin");
                        +it
                    }
                }
                help.appendChild(answer)
                jq(answer).hide()
            }
        }
        CodeMirror.colorize(help.getElementsByTagName("code"))

        helpWidget = cmDocument.addLineWidget(0, helpWrapper, json("above" to true, "noHScroll" to true))

        if (file.taskWindows.isEmpty() || file.isModified) return
        val firstWindow = file.taskWindows.first()
        cmDocument.setSelection(
                Position(firstWindow.line, firstWindow.start + firstWindow.length),
                Position(firstWindow.line, firstWindow.start)
        )
        for (taskWindow in file.taskWindows) {
            cmDocument.markText(
                    Position(taskWindow.line, taskWindow.start),
                    Position(taskWindow.line, taskWindow.start + taskWindow.length),
                    json(
                            "className" to "taskWindow",
                            "startStyle" to "taskWindow-start",
                            "endStyle" to "taskWindow-end",
                            "handleMouseEvents" to true
                    )
            )
        }
    }
}

private fun getCompletions(cm: CodeMirror, callback: (Hint) -> Unit, options: dynamic) {
    val cur = cm.getCursor()
    val token = cm.getTokenAt(cur)

    fun processCompletionsList(completions: List<CompletionView>) {
        val hint = Hint(
                Position(cur.line, token.start),
                Position(cur.line, token.end),
                completions.toTypedArray()
        )

        callback(hint)
    }

    if (storedCompletionsList != null) {
        val list =
                if ((token.string == ".") || (token.string == " ") || (token.string == "(")) {
                    storedCompletionsList!!
                } else {
                    storedCompletionsList!!.filter { it.text.startsWith(token.string) }
                }
        processCompletionsList(list)
    } else {
        Application.completionProvider.getCompletion(
                Application.accordion.selectedProjectView!!.project,
                openedFile!!.name,
                cur,
                { completionProposals ->
                    storedCompletionsList = completionProposals.map(::CustomizedCompletionView)
                    processCompletionsList(storedCompletionsList!!)
                }
        )
    }


}

public fun showDiagnostics(diagnostics: Map<File, List<Diagnostic>>) {
    removeStyles()
    for (entry in diagnostics) {
        val relatedDocument = documents[entry.key]!!
        for (diagnostic in entry.value) {
            val interval = diagnostic.interval
            val errorMessage = unEscapeString(diagnostic.message)
            val severity = diagnostic.severity

            arrayClasses.add(relatedDocument.markText(interval.start, interval.end, json(
                    "className" to diagnostic.className,
                    "title" to errorMessage
            )))

            if (relatedDocument.getEditor() !== codeMirror) continue

            if ((codeMirror.lineInfo(interval.start.line) != null) && (codeMirror.lineInfo(interval.start.line).gutterMarkers == null)) {
                codeMirror.setGutterMarker(interval.start.line, "errors-and-warnings-gutter", document.create.div {
                    classes = setOf(severity + "gutter")
                    title = errorMessage
                })
            } else {
                val gutter: HTMLElement = codeMirror.lineInfo(interval.start.line).gutterMarkers["errors-and-warnings-gutter"]
                gutter.title += "\n$errorMessage"
                if (gutter.className.indexOf("ERRORgutter") == -1) {
                    gutter.className = severity + "gutter"
                }
            }
        }
    }
}

fun removeStyles() {
    arrayClasses.forEach { it.clear() }
    codeMirror.clearGutter("errors-and-warnings-gutter")
}

private var isLoadingHighlighting = false
private fun getHighlighting() {
    if (highlightOnTheFly && openedFile != null && !isLoadingHighlighting) {
        isLoadingHighlighting = true
        var example = Application.accordion.selectedProjectView!!.project
        Application.highlightingProvider.getHighlighting(example, { data -> showDiagnostics(data) }, { isLoadingHighlighting = false })
    }
}

fun openDialog(template: HTMLElement, callback: () -> Unit, options: dynamic): (() -> Unit) {
    val closeFunction = codeMirror.openDialog(template, callback, options)

    var closed = false
    val safeCloseFunction = {
        if(!closed) {
            closeFunction()
            closed = true
        }
    }

    dialogCloseFunctions.add(safeCloseFunction)
    return safeCloseFunction;
}


}
