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

/**
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/30/12
 * Time: 3:37 PM
 */

var KotlinEditor = (function () {
    function KotlinEditor() {

        var instance = {
            refresh: function () {
                my_editor.refresh();
            },
            setCursor: function (lineNo, charNo) {
                my_editor.setCursor(lineNo, charNo);
            },
            focus: function () {
                my_editor.focus()
            },
            highlightOnTheFly: function (flag) {
                highlightOnTheFly = flag;
            },
            getText: function () {
                return my_editor.getValue();
            },
            open: function (file) {
                $(runButton).button("option", "disabled", false);
                document.getElementById("workspace-overlay").style.display = "none";
                if (file.type != FileType.JAVA_FILE) {
                    my_editor.setOption("mode", "text/kotlin")
                } else {
                    my_editor.setOption("mode", "text/x-java")
                }

                if (openedFile == null) {
                    openedFile = file;
                    highlighting.removeStyles();
                    if (!openedFile.isModifiable) {
                        my_editor.setOption("readOnly", true);
                    } else {
                        my_editor.setOption("readOnly", false);
                    }
                    my_editor.focus();
                    my_editor.setValue(openedFile.text);
                    openedFile.changesHistory != null ? my_editor.setHistory(openedFile.changesHistory) : my_editor.clearHistory();
                    highlighting.updateHighlighting();
                } else {
                    throw("Previous file wasn't closed");
                }
            },
            closeFile: function () {
                if (openedFile != null) {
                    openedFile.changesHistory = my_editor.getHistory();
                    openedFile = null;
                }
                highlighting.removeStyles();
                my_editor.clearHistory();
                my_editor.setValue("");
                $(runButton).button("option", "disabled", true);
                document.getElementById("workspace-overlay").style.display = "block";
            },
            reloadFile: function () {
                if (openedFile != null) {
                    my_editor.focus();
                    my_editor.setValue(openedFile.text);
                    openedFile.changesHistory != null ? my_editor.setHistory(openedFile.changesHistory) : my_editor.clearHistory();
                    highlighting.updateHighlighting();
                }
            },
            onCursorActivity: function (cursorPosition) {
            },
            getWordAtCursor: function (cursorPosition) {
                var word = my_editor.getTokenAt(cursorPosition).string;
                if (checkDataForNull(word)) {
                    return word;
                }
                return "";
            },
            getMessageForLineAtCursor: function (cursorPosition) {
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
            },
            cursorCoords: function () {
                return my_editor.cursorCoords();
            },
            updateHighlighting: function () {
                highlighting.updateHighlighting()
            },
            /**
             * For the debug purposes.
             */
            setValue: function (text) {
                my_editor.setValue(text);
            }
        };

        var my_editor;
        var openedFile = null;
        var highlightOnTheFly = false;

        var HighlightingObject = (function () {
            function HighlightingObject() {
                var instance = {
                    updateHighlighting: function () {
                        updateHighlighting()
                    },
                    removeStyles: function () {
                        for (var i = 0; i < arrayClasses.length; ++i) {
                            arrayClasses[i].clear();
                        }
                        my_editor.clearGutter("errors-and-warnings-gutter");
                    }
                };

                var arrayClasses = [];

                function createGutterElement(severity, title) {
                    var element = document.createElement("div");
                    element.className = severity + "gutter";
                    element.title = title;
                    return element;
                }

                function updateHighlighting() {
                    instance.removeStyles();
                    for (var i = 0; i < openedFile.errors.length; i++) {
                        var error = openedFile.errors[i];
                        var interval = error.interval;
                        var title = unEscapeString(error.message);
                        var severity = error.severity;

                        arrayClasses.push(my_editor.markText(interval.start, interval.end, {
                            className: error.className,
                            title: title
                        }));

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

                return instance;
            }

            return HighlightingObject;
        })();

        var highlighting = new HighlightingObject();

        var storedCompletionProposalsList = null;
        function getCompletions(cm, callback, options) {
            var cur = cm.getCursor();
            var token = cm.getTokenAt(cur);
            var completion = {
                from: {line: cur.line, ch: token.start},
                to: {line: cur.line, ch: token.end},
                list: []
            };

            //Fired when the completion is finished
            CodeMirror.on(completion, "close", function () {
                storedCompletionProposalsList = null;
            });

            if (storedCompletionProposalsList != null) {
                var filteredCompletions;
                if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                    filteredCompletions = storedCompletionProposalsList;
                } else {
                    filteredCompletions = [];
                    $(storedCompletionProposalsList).each(function (ind, element) {
                        if (element.text.startsWith(token.string)) {
                            filteredCompletions.push(element);
                        }
                    });
                }
                completion.list = filteredCompletions;
                callback(completion)
            } else {
                completionProvider.getCompletion(
                    accordion.getSelectedProject(),
                    openedFile.name,
                    cur,
                    function (data) {
                        $(data).each(function (idx, element) {
                            element.render = renderCompletion;
                            element.hint = applyCompletion;
                            completion.list.push(element)
                        });

                        //Fired before completion update. getCompletions will be called after
                        //Stored list will be reused in getCompletion calls and deleted when completion is finished
                        CodeMirror.on(completion, "update", function () {
                            storedCompletionProposalsList = completion.list;
                        });

                        callback(completion)
                    })
            }

            function renderCompletion(element, self, data) {
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

            function applyCompletion(cm, self, data) {
                var token = cm.getTokenAt(cm.getCursor());
                var from;
                var to = {line: cur.line, ch: token.end};
                if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                    from = to;
                    cm.replaceRange(data.text, from);
                } else {
                    from = {line: cur.line, ch: token.start};
                    cm.replaceRange(data.text, from, to);
                    if (data.text.endsWith('(')) {
                        cm.replaceRange(")", {line: cur.line, ch: token.start + data.text.length});
                        cm.execCommand("goCharLeft")
                    }
                }
            }
        }

        CodeMirror.registerHelper("hint", "kotlin", getCompletions);

        my_editor = CodeMirror.fromTextArea(document.getElementById("code"), {
            lineNumbers: true,
            styleActiveLine: true,
            matchBrackets: true,
            mode: "text/kotlin",
            autoCloseBrackets: true,
            continueComments: true,
            hintOptions: {async: true},
            gutters: ["errors-and-warnings-gutter"],
            indentUnit: 4
        });

        if (navigator.appVersion.indexOf("Mac") != -1) {
            my_editor.setOption("extraKeys", {
                "Cmd-Alt-L": "indentAuto",
                "Ctrl-Space": function (mirror) {
                    CodeMirror.commands.autocomplete(mirror, CodeMirror.hint.kotlin, {async: true})
                },
                "Shift-Tab": "indentLess",
                "Ctrl-/": "toggleComment",
                "Cmd-[": false,
                "Cmd-]": false
            })
        } else {
            my_editor.setOption("extraKeys", {
                "Ctrl-Alt-L": "indentAuto",
                "Ctrl-Space": function (mirror) {
                    CodeMirror.commands.autocomplete(mirror, CodeMirror.hint.kotlin, {async: true})
                },
                "Shift-Tab": "indentLess",
                "Ctrl-/": "toggleComment",
                "Ctrl-[": false,
                "Ctrl-]": false
            })
        }


        my_editor.on("change", function () {
            highlighting.removeStyles();
            if (openedFile != null) {
                openedFile.text = my_editor.getValue();
                if (timer) {
                    clearTimeout(timer);
                    timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
                }
                else {
                    timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
                }
            }

        });

        my_editor.on("cursorActivity", function (codemirror) {
            instance.onCursorActivity(codemirror.getCursor());
        });

        var timer;
        var timerIntervalForNonPrinting = 300;

        var isLoadingHighlighting = false;

        function getHighlighting() {
            if (highlightOnTheFly && openedFile != null && !isLoadingHighlighting) {
                isLoadingHighlighting = true;
                var example = accordion.getSelectedProject();
                highlightingProvider.getHighlighting(example, highlighting.updateHighlighting, function () {
                    isLoadingHighlighting = false
                });
            }
        }

        return instance;
    }


    return KotlinEditor;
})();