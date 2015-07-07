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

import application.Application
import html4k.dom.create
import html4k.js.div
import jquery.jq
import model.File
import model.FileType
import model.Task
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import providers.HelpProvider
import utils.codemirror.CodeMirror
import utils.codemirror.CompletionView
import utils.codemirror.Hint
import utils.codemirror.Position
import utils.jquery.JQuery
import utils.jquery.children
import utils.jquery.find
import utils.jquery.toArray
import utils.unEscapeString
import java.util.*
import kotlin.browser.document
import kotlin.browser.window

class Editor(
        private val helpProvider: HelpProvider
) {
    val codeMirror = CodeMirror.fromTextArea(document.getElementById("code") as HTMLTextAreaElement, json(
            "lineNumbers" to true,
            "styleActiveLine" to true,
            "matchBrackets" to true,
            "mode" to "kotlin",
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

    init {
        var timeoutId: Int? = null
        codeMirror.on("change", {
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

        codeMirror.on("blur", {
            window.clearTimeout(helpTimeout)
            HelpViewForWords.hide()
        })

        codeMirror.on("endCompletion") {
            storedCompletionsList = null
        }

        CodeMirror.registerHelper("hint", "kotlin", { cm: dynamic, callback: dynamic, options: dynamic ->
            getCompletions(cm, callback, options)
        })

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
    fun setCursor (lineNo: Int, charNo: Int) {
        codeMirror.setCursor(lineNo, charNo)
    }
    fun focus() {
        codeMirror.focus()
    }

    fun open(file: File) {
        Application.runButton.disabled = false
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "none"

        file.project.files.forEach { createDocIfNotExist(it) }
        val relatedDocument = documents.get(file)!!
        if (openedFile == null) {
            openedFile = file
            removeStyles()
            codeMirror.setOption("readOnly", !openedFile!!.isModifiable)
            codeMirror.focus()
            codeMirror.swapDoc(relatedDocument)
            for(closeFunction in dialogCloseFunctions){
                closeFunction()
            }
            dialogCloseFunctions.clear()
            codeMirror.refresh()
            Application.accordion.onModifiedSelectedFile(file)
        } else {
            throw Exception("Previous file wasn't closed")
        }
    }
    fun closeFile () {
        codeMirror.swapDoc(CodeMirror.Doc(""))
        openedFile = null
        removeStyles()
        Application.runButton.disabled = true
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "block"
    }
    fun reloadFile () {
        val openedFile = this.openedFile;
        this.openedFile = null
        documents.remove(openedFile);
        if (openedFile != null) open(openedFile);
    }

    fun updateHighlighting(){
        getHighlighting()
    }

    private fun createDocIfNotExist(file: File) {
        if (documents.get(file) == null) {
            val type = if (file.type != FileType.JAVA_FILE.name()) {
                "kotlin"
            } else {
                "text/x-java"
            }
            val cmDocument = CodeMirror.Doc(file.userText, type);
            documents.put(file, cmDocument);
            getHighlighting()


            if (!(file.project is Task && file.name == "Task.kt")) return
            val help = document.create.div("taskHelp")
            val helpContent = JQuery.parseHTML(file.project.help)
            helpContent?.forEach { help.appendChild(it) }
            jq(help).find("a").attr("target", "_blank")
            CodeMirror.colorize(help.getElementsByTagName("code"))
            cmDocument.addLineWidget(0, help, json("above" to true, "noHScroll" to true))

            if (file.project.taskWindows.isEmpty() || file.isModified) return
            val firstWindow = file.project.taskWindows.first()
            cmDocument.setSelection(
                    Position(firstWindow.line, firstWindow.start + firstWindow.length),
                    Position(firstWindow.line, firstWindow.start)
            )
            for(taskWindow in file.project.taskWindows){
                cmDocument.markText(
                        Position(taskWindow.line, taskWindow.start),
                        Position(taskWindow.line, taskWindow.start + taskWindow.length),
                        json(
                                "className" to "taskWindow",
                                "startStyle" to "taskWindow-start",
                                "clearOnEnter" to true,
                                "endStyle" to "taskWindow-end",
                                "handleMouseEvents" to true,
                                "inclusiveLeft" to true,
                                "inclusiveRight" to true
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

    public fun showDiagnostics(diagnostics: Map<File, Array<Diagnostic>>) {
        removeStyles()
        for (entry in diagnostics) {
            val relatedDocument = documents.get(entry.getKey())!!
            for (diagnostic in entry.getValue()) {
                val interval = diagnostic.interval
                val errorMessage = unEscapeString(diagnostic.message)
                val severity = diagnostic.severity

                arrayClasses.add(relatedDocument.markText(interval.start, interval.end, json(
                        "className" to diagnostic.className,
                        "title" to errorMessage
                )))

                if(relatedDocument.getEditor() !== codeMirror) continue

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

    fun openDialog(template: HTMLElement, callback: () -> Unit, options: dynamic): (() -> Unit){
        val closeFunction = codeMirror.openDialog(template, callback, options)
        dialogCloseFunctions.add(closeFunction)
        return closeFunction;
    }


}
