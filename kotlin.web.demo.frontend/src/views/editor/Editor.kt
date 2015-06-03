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
import jquery.jq
import model.File
import model.FileType
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import utils.codemirror.CodeMirror
import utils.codemirror.Position
import utils.jquery.ui.button
import utils.unEscapeString
import views.editor.Completion
import kotlin.browser.document
import kotlin.browser.window
import html4k.*
import html4k.js.*
import html4k.dom.*
import views.editor.Error
import utils.Object
import kotlin.browser

class Editor(
        private val onCursorActivity: (dynamic) -> Unit
) {
    val my_editor = CodeMirror.fromTextArea(document.getElementById("code") as HTMLTextAreaElement, json(
            "lineNumbers" to true,
            "styleActiveLine" to true,
            "matchBrackets" to true,
            "mode" to "text/kotlin",
            "autoCloseBrackets" to true,
            "continueComments" to true,
            "hintOptions" to json("async" to true),
            "gutters" to arrayOf("errors-and-warnings-gutter"),
            "indentUnit" to 4
    ));
    var openedFile: File? = null
    private val timerIntervalForNonPrinting = 300;
    var highlightOnTheFly = false;
    var arrayClasses = arrayListOf<dynamic>();
    private val documents = hashMapOf<String, CodeMirror.Doc>()

    init {
        var timeoutId: Int? = null;
        my_editor.on("change", {
            removeStyles();
            if (openedFile != null) {
                openedFile!!.text = my_editor.getValue();
                if (timeoutId != null) {
                    window.clearTimeout(timeoutId ?: 0);
                    timeoutId = window.setTimeout({ getHighlighting() }, timerIntervalForNonPrinting);
                } else {
                    timeoutId = window.setTimeout({ getHighlighting() }, timerIntervalForNonPrinting);
                }
            }

        });

        my_editor.on("cursorActivity", { codemirror ->
            onCursorActivity(codemirror.getCursor());
        });

        CodeMirror.registerHelper("hint", "kotlin", { cm: dynamic, callback: dynamic, options: dynamic ->
            getCompletions(cm, callback, options)
        }) ;

        if (window.navigator.appVersion.indexOf("Mac") != -1) {
            my_editor.setOption("extraKeys", json(
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
            my_editor.setOption("extraKeys", json(
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
        my_editor.refresh();
    }
    fun setCursor (lineNo: Int, charNo: Int) {
        my_editor.setCursor(lineNo, charNo);
    }
    fun focus() {
        my_editor.focus()
    }

    fun getText(): String {
        return my_editor.getValue();
    }
    fun open(file: File) {
        Application.runButton.disabled = false;
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "none";

        file.project.files.forEach { createDocIfNotExist(it) }
        val relatedDocument = documents.get(file.id)!!
        if (openedFile == null) {
            openedFile = file;
            removeStyles();
            my_editor.setOption("readOnly", !openedFile!!.isModifiable);
            my_editor.focus();
            my_editor.swapDoc(relatedDocument);
            Application.accordion.onModifiedSelectedFile(file);
        } else {
            throw Exception("Previous file wasn't closed");
        }
    }
    fun closeFile () {
        my_editor.swapDoc(CodeMirror.Doc(""))
        openedFile = null;
        removeStyles();
        Application.runButton.disabled = true;
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "block";
    }
    fun reloadFile () {
        if (openedFile != null) {
            my_editor.focus();
            my_editor.setValue(openedFile!!.text);
            updateHighlighting();
        }
    }

    fun getWordAtCursor(cursorPosition: dynamic) = my_editor.getTokenAt(cursorPosition).string;

    fun cursorCoords() = my_editor.cursorCoords()

    fun updateHighlighting(){
        getHighlighting()
    }

    private fun createDocIfNotExist(file: File) {
        if (documents.get(file.id) == null) {
            val type = if (file.type != FileType.JAVA_FILE.name()) {
                "text/kotlin"
            } else {
                "text/x-java"
            }
            documents.put(file.id, CodeMirror.Doc(file.text, type))
            getHighlighting()
        }
    }

    private var storedCompletionsList: Array<Completion>? = null;
    private fun getCompletions(cm: CodeMirror, callback: (Hint) -> Unit, options: dynamic) {
        var cur = cm.getCursor();
        var token = cm.getTokenAt(cur);
        var hint = Hint(
                Position(cur.line, token.start),
                Position(cur.line, token.end),
                arrayOf<Completion>()
        );

        //Fired when the completion is finished
        CodeMirror.on(hint, "close", {
            storedCompletionsList = null;
            Unit
        });

        if (storedCompletionsList != null) {
            hint.list =
                    if ((token.string == ".") || (token.string == " ") || (token.string == "(")) {
                        storedCompletionsList!!;
                    } else {
                        storedCompletionsList!!.filter { it.text.startsWith(token.string) }.toTypedArray()
                    }
            callback(hint)
        } else {
            Application.completionProvider.getCompletion(
                    Application.accordion.selectedProjectView!!.project,
                    openedFile!!.name,
                    cur,
                    { completionProposals ->
                        hint.list = completionProposals.map { Completion(it) }.toTypedArray()
                        //Fired before completion update. getCompletions will be called after
                        //Stored list will be reused in getCompletion calls and deleted when completion is finished
                        CodeMirror.on(hint, "update", {
                            storedCompletionsList = hint.list;
                            Unit
                        });
                        callback(hint)
                    }
            )
        }


    }

    public fun setHighlighting(errors: Map<String, Array<Error>>) {
        removeStyles();
        for (fileId in errors.keySet()) {
            val relatedDocument = documents.get(fileId)!!
            for (error in errors[fileId]) {
                var interval = error.interval;
                var errorMessage = unEscapeString(error.message);
                var severity = error.severity;

                arrayClasses.add(relatedDocument.markText(interval.start, interval.end, json(
                        "className" to error.className,
                        "title" to errorMessage
                )));

                if ((my_editor.lineInfo(interval.start.line) != null) && (my_editor.lineInfo(interval.start.line).gutterMarkers == null)) {
                    my_editor.setGutterMarker(interval.start.line, "errors-and-warnings-gutter", document.create.div {
                        classes = setOf(severity + "gutter")
                        title = errorMessage
                    });
                } else {
                    var gutter: HTMLElement = my_editor.lineInfo(interval.start.line).gutterMarkers["errors-and-warnings-gutter"];
                    gutter.title += "\n" + errorMessage;
                    if (gutter.className.indexOf("ERRORgutter") == -1) {
                        gutter.className = severity + "gutter";
                    }
                }

                var el = document.getElementById(interval.start.line + "_" + interval.start.ch);
                if (el != null) {
                    el.setAttribute("title", errorMessage);
                }
            }
        }
    }

    fun removeStyles() {
        arrayClasses.forEach { it.clear() }
        my_editor.clearGutter("errors-and-warnings-gutter");
    }

    private var isLoadingHighlighting = false;
    private fun getHighlighting() {
        if (highlightOnTheFly && openedFile != null && !isLoadingHighlighting) {
            isLoadingHighlighting = true;
            var example = Application.accordion.selectedProjectView!!.project;
            Application.highlightingProvider.getHighlighting(example, { data -> setHighlighting(data) }, { isLoadingHighlighting = false });
        }
    }

}
