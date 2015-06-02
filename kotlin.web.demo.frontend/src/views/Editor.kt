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

package views

import application.Application
import jquery.jq
import model.File
import model.FileType
import org.w3c.dom.HTMLElement
import utils.unEscapeString
import kotlin.browser.document
import kotlin.browser.window

class Editor(
        private val onCursorActivity: (dynamic) -> Unit
) {
    val my_editor = CodeMirror.fromTextArea(document.getElementById("code"), json(
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
    private var timerIntervalForNonPrinting = 300;
    var highlightOnTheFly = false;
    var arrayClasses = arrayListOf<dynamic>();

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
    fun getText() {
        return my_editor.getValue();
    }
    fun open(file: File) {
        jq(Application.runButtonElement).button("option", "disabled", false);
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "none";
        if (file.type != FileType.JAVA_FILE.name()) {
            my_editor.setOption("mode", "text/kotlin")
        } else {
            my_editor.setOption("mode", "text/x-java")
        }

        if (openedFile == null) {
            openedFile = file;
            removeStyles();
            if (!openedFile!!.isModifiable) {
                my_editor.setOption("readOnly", true);
            } else {
                my_editor.setOption("readOnly", false);
            }
            my_editor.focus();
            my_editor.setValue(openedFile!!.text);
            if(openedFile!!.changesHistory != null) my_editor.setHistory(openedFile!!.changesHistory) else my_editor.clearHistory();
            updateHighlighting();
            Application.accordion.onModifiedSelectedFile(file);
        } else {
            throw Exception("Previous file wasn't closed");
        }
    }
    fun closeFile () {
        if (openedFile != null) {
            openedFile!!.changesHistory = my_editor.getHistory();
            openedFile = null;
        }
        removeStyles();
        my_editor.clearHistory();
        my_editor.setValue("");
        jq(Application.runButtonElement).button("option", "disabled", true);
        (document.getElementById("workspace-overlay") as HTMLElement).style.display = "block";
    }
    fun reloadFile () {
        if (openedFile != null) {
            my_editor.focus();
            my_editor.setValue(openedFile!!.text);
            if(openedFile!!.changesHistory != null) my_editor.setHistory(openedFile!!.changesHistory) else my_editor.clearHistory();
            updateHighlighting();
        }
    }
    fun getWordAtCursor (cursorPosition: dynamic): String {
        var word = my_editor.getTokenAt(cursorPosition).string;
        if (word != null) {
            return word;
        }
        return "";
    }
    fun getMessageForLineAtCursor (cursorPosition: dynamic): String {
        var message = "";
        var lineNumber = cursorPosition.line;
        var text = my_editor.lineInfo(lineNumber).markerText;
        if (text != null) {
            text = text.substring(text.indexOf("title=\"") + 7);
            text = text.substring(0, text.indexOf("\""));
            if (text.length > 90) text = text.substring(0, 90) + "...";
            message = "line " + (lineNumber + 1) + " - " + text;
        }
        return message;
    }

    fun cursorCoords() {
        return my_editor.cursorCoords();
    }

    fun updateHighlighting(){
        getHighlighting()
    }

    fun removeHighlighting() {
        removeStyles()
    }

    fun setValue (text: String) {
        my_editor.setValue(text);
    }

    var storedCompletionProposalsList: dynamic = null;
    private fun getCompletions(cm: dynamic, callback: dynamic, options: dynamic) {
        var cur = cm.getCursor();
        var token = cm.getTokenAt(cur);
        var completion: dynamic = json(
                "from" to json("line" to cur.line, "ch" to token.start),
                "to" to json("line" to cur.line, "ch" to token.end),
                "list" to arrayOf<dynamic>()
        );

        //Fired when the completion is finished
        CodeMirror.on(completion, "close", {
            storedCompletionProposalsList = null;
            Unit
        });

        if (storedCompletionProposalsList != null) {
            var filteredCompletions: dynamic = null;
            if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                filteredCompletions = storedCompletionProposalsList;
            } else {
                filteredCompletions = arrayOf<dynamic>();
                storedCompletionProposalsList.forEach({ element ->
                    if (element.text.startsWith(token.string)) {
                        filteredCompletions.push(element);
                    }
                });
            }
            completion.list = filteredCompletions;
            callback(completion)
        } else {
            Application.completionProvider.getCompletion(
                    Application.accordion.selectedProjectView!!.project,
                    openedFile!!.name,
                    cur,
                    { data ->
                        data.forEach { element ->
                            element.render = { element: dynamic, self: dynamic, data: dynamic ->
                                var icon = document.createElement("div");
                                icon.className = "icon " + data.icon;
                                element.appendChild(icon);

                                var textSpan = document.createElement("div");
                                textSpan.className = "name";
                                textSpan.innerHTML = data.displayText;
                                element.appendChild(textSpan);

                                var tail = document.createElement("div");
                                tail.className = "tail";
                                tail.innerHTML = data.tail;
                                element.appendChild(tail);
                            }
                            element.hint = { cm: dynamic, self: dynamic, data: dynamic ->
                                val token = cm.getTokenAt(cm.getCursor());
                                var from: dynamic = null;
                                var to: dynamic = json("line" to cur.line, "ch" to token.end);
                                if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                                    from = to;
                                    cm.replaceRange(data.text, from);
                                } else {
                                    from = json("line" to cur.line, "ch" to token.start);
                                    cm.replaceRange(data.text, from, to);
                                    if (data.text.endsWith('(')) {
                                        cm.replaceRange(")", json("line" to cur.line, "ch" to token.start + data.text.length));
                                        cm.execCommand("goCharLeft")
                                    }
                                }
                            };
                            completion.list.push(element)
                        }

                        //Fired before completion update. getCompletions will be called after
                        //Stored list will be reused in getCompletion calls and deleted when completion is finished
                        CodeMirror.on(completion, "update", {
                            storedCompletionProposalsList = completion.list;
                            Unit
                        });

                        callback(completion)
                    })
        }


    }

    private fun createGutterElement(severity: dynamic, title: String): HTMLElement {
        var element = document.createElement("div") as HTMLElement;
        element.className = severity + "gutter";
        element.title = title;
        return element;
    }

    fun setHighlighting() {
        removeStyles();
        for (error in openedFile!!.errors) {
            var interval = error.interval;
            var title = unEscapeString(error.message);
            var severity = error.severity;

            arrayClasses.add(my_editor.markText(interval.start, interval.end, json(
                    "className" to error.className,
                    "title" to title
            )));

            if ((my_editor.lineInfo(interval.start.line) != null) && (my_editor.lineInfo(interval.start.line).gutterMarkers == null)) {
                my_editor.setGutterMarker(interval.start.line, "errors-and-warnings-gutter", createGutterElement(severity, title));
            } else {
                var gutter = my_editor.lineInfo(interval.start.line).gutterMarkers["errors-and-warnings-gutter"];
                gutter.title += "\n" + title;
                if (gutter.className.indexOf("ERRORgutter") == -1) {
                    gutter.className = severity + "gutter";
                }
            }

            var el = document.getElementById(interval.start.line + "_" + interval.start.ch);
            if (el != null) {
                el.setAttribute("title", title);
            }
        }
    }

    private fun removeStyles() {
        arrayClasses.forEach { it.clear() }
        my_editor.clearGutter("errors-and-warnings-gutter");
    }

    private var isLoadingHighlighting = false;
    private fun getHighlighting() {
        if (highlightOnTheFly && openedFile != null && !isLoadingHighlighting) {
            isLoadingHighlighting = true;
            var example = Application.accordion.selectedProjectView!!.project;
            Application.highlightingProvider.getHighlighting(example, { setHighlighting() }, { isLoadingHighlighting = false });
        }
    }

}

