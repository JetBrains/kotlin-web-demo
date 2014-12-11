/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

var COMPLETION_ISNOT_AVAILABLE = "Switch to \"Client\" or \"Server\" mode to enable completion";


var KotlinEditor = (function () {
    function KotlinEditor() {
        var my_editor;
        var openedFile = null;
        var highlightOnTheFly = false;

        var CompletionObject = (function () {
            var keywords;
            var isContinueComplete = false;

            var sel = $("#selectId");

            function close() {
                sel.css("display", "none");
                sel.empty();
            }

            sel.menu();
            close();
            sel.on("keydown", function (event) {
                var code = event.keyCode;

                // Enter and space
                if (code == 13 || code == 32) {
                    isContinueComplete = false;
                }
                // Escape
                else if (code == 27) {
                    isContinueComplete = false;
                    event.stopPropagation();
                    close();
                    my_editor.focus();
                } else if (code == 8) {
                    event.preventDefault();
                    event.stopPropagation();
                    close();
                    my_editor.focus();
                    my_editor.deleteH(-1, "char");
                    setTimeout(continueComplete, 50);
                } else if (code != 38 && code != 40) {
                    close();
                    my_editor.focus();
                    setTimeout(continueComplete, 50);
                }
            });

            sel.on("blur", close);


            function CompletionObject() {
                var instance = {
                    processCompletionResult: function (data) {
                        startComplete(data);
                    }
                };

                return instance;
            }

            function LookupElement(name, tail, icon) {
                this.icon = icon;
                this.name = name;
                this.tail = tail;
            }

            function continueComplete() {
                isContinueComplete = true;
                startComplete(null);
            }


            function startComplete(data) {
                if (my_editor.somethingSelected()) return;
                var cur = my_editor.getCursor(null);
                var token = my_editor.getTokenAt(cur);

                if ((data != null) && (data != undefined)) {
                    if (!isContinueComplete) {
                        keywords = [];
                    }
                    if (data == COMPLETION_ISNOT_AVAILABLE) {
                        keywords.push(new LookupElement(COMPLETION_ISNOT_AVAILABLE, "", ""));
                    } else {
                        var i = 0;
                        while (data[i] != undefined) {
                            var lookupElement = new LookupElement(data[i].name, data[i].tail, data[i].icon);
                            keywords.push(lookupElement);
                            i++;
                        }
                    }
                } else {
                    if (!isContinueComplete) {
                        keywords = [];
                        return;
                    }
                    isContinueComplete = false;
                }
                var completions;
                if (data == COMPLETION_ISNOT_AVAILABLE) {
                    completions = getCompletions(COMPLETION_ISNOT_AVAILABLE);
                } else {
                    completions = getCompletions(token);
                }
                if ((completions.length == 0) || (completions == null)) return;
                if (completions.length == 1 && !isContinueComplete && data != COMPLETION_ISNOT_AVAILABLE) {
                    insert(completions[0].name);
                    return;
                }


                function insert(str) {
                    if (str != undefined) {
                        var position = str.indexOf("(");
                        if (position != undefined) {
                            if (position != -1) {
                                //If this is a string with a package after
                                if (str.charAt(position - 1) == ' ') {
                                    position = position - 2;
                                }
                                //if this is a method without args
                                if (str.charAt(position + 1) == ')') {
                                    position++;
                                }
                                str = str.substring(0, position + 1);
                            }
                        }
                        position = str.indexOf(":");
                        if (position != undefined) {
                            if (position != -1) {
                                str = str.substring(0, position - 1);
                            }
                        }
                        if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                            my_editor.replaceRange(str, {line: cur.line, ch: token.end}, {
                                line: cur.line,
                                ch: token.end
                            });
                        } else {
                            my_editor.replaceRange(str, {line: cur.line, ch: token.start}, {
                                line: cur.line,
                                ch: token.end
                            });
                        }
                    }
                }

                sel.unbind("menuselect");
                sel.on("menuselect", function (event, ui) {
                    var text = ui.item.children().children()[1].innerHTML;
                    isContinueComplete = false;
                    if (text == COMPLETION_ISNOT_AVAILABLE) {
                        close();
                        my_editor.focus();
                        return;
                    }
                    insert(text);
                    close();
                    setTimeout(function () {
                        my_editor.focus();
                    }, 50);
                });


                sel.empty();

                var i = 0;
                for (i = 0; i < completions.length; ++i) {
                    var opt = document.createElement("li");
                    var pEl = document.createElement("p");
                    pEl.className = "lookupElement";

                    var icon = document.createElement("div");
                    if (completions[i].icon != "") {
                        icon.className = "lookupElementIcon " + completions[i].icon + "-icon";
                    }
                    pEl.appendChild(icon);

                    var spanName = document.createElement("div");
                    spanName.className = "lookupElementName";
                    spanName.innerHTML = completions[i].name;

                    var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
                    if (is_chrome && data != COMPLETION_ISNOT_AVAILABLE) {
                        spanName.innerHTML += " : ";
                    }
                    pEl.appendChild(spanName);

                    var spanTail = document.createElement("div");
                    spanTail.className = "lookupElementTail";
                    spanTail.innerHTML = completions[i].tail;
                    pEl.appendChild(spanTail);
                    opt.appendChild(pEl);
                    if (i == 0) {
                        opt.id = "selected";
                    }
                    sel.append(opt);
                }

                var pos = my_editor.cursorCoords();
                sel.css("position", "absolute");
                sel.css("left", pos.left + 2 + "px");
                sel.css("top", pos.top + 15 + "px");

                sel.menu("refresh");
                sel.css("display", "block");
                sel.focus();
                sel.menu("focus", null, sel.find(".ui-menu-item:first"));


            }


            function getCompletions(token) {
                var start;
                if (token == COMPLETION_ISNOT_AVAILABLE) {
                    start = token;
                } else {
                    start = token.string;
                }
                var found = [];

                function maybeAdd(lookupElement) {
                    if (lookupElement.name.indexOf(start) == 0) found.push(lookupElement);
                }

                function add(str) {
                    found.push(str);
                }

                if (keywords == undefined) return found;
                if ((start.indexOf(' ') == 0) || (start == '.')) {
                    forEachInArray(keywords, add);
                } else {
                    forEachInArray(keywords, maybeAdd);
                }

                return found;
            }

            // Minimal event-handling wrapper.
            function stopEvent() {
                if (this.preventDefault) {
                    this.preventDefault();
                    this.stopPropagation();
                }
                else {
                    this.returnValue = false;
                    this.cancelBubble = true;
                }
            }

            function addStop(event) {
                if (!event.stop) event.stop = stopEvent;
                return event;
            }

            function connect(node, type, handler) {
                function wrapHandler(event) {
                    handler(addStop(event || window.event));
                }

                if (typeof node.addEventListener == "function")
                    node.addEventListener(type, wrapHandler, false);
                else
                    node.attachEvent("on" + type, wrapHandler);
            }

            return CompletionObject;
        })();

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
                    for (var i = 0; i < openedFile.getErrors().length; i++) {
                        var error = openedFile.getErrors()[i];
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


        var completion = new CompletionObject();
        var highlighting = new HighlightingObject();

        var instance = {
            resize: function () {
                var workspaceHeight = $("#workspace").height();
                var toolBoxHeight = $("#toolbox").outerHeight();
                var commandLineArgumentsHeight = $(argumentsWrapper).is(':visible') ? $(argumentsWrapper).outerHeight() : 0;
                var notificationsHeight = $("#editor-notifications").is(':visible') ? $("#editor-notifications").outerHeight() : 0;
                var editorHeight = workspaceHeight - toolBoxHeight - commandLineArgumentsHeight - notificationsHeight;
                $("#editorinput").find(".CodeMirror").css("height", editorHeight);
                my_editor.refresh();
            },
            setCursor: function (lineNo, charNo) {
                my_editor.setCursor(lineNo, charNo);
            },
            focus: function () {
                my_editor.focus()
            },
            showCompletionResult: function (data) {
                completion.processCompletionResult(data);
            },
            highlightOnTheFly: function (flag) {
                highlightOnTheFly = flag;
            },
            getText: function () {
                return my_editor.getValue();
            },
            indentAll: function () {
                my_editor.setSelection({line: 0, ch: 0}, {line: my_editor.lineCount() - 1, ch: 0});
                my_editor.indentSelection("smart");
            },
            refreshMode: function () {
                my_editor.setOption("mode", "kotlin");
            },
            open: function (file) {
                document.getElementById("workspace-overlay").style.display = "none";

                if (openedFile == null) {
                    openedFile = file;
                    highlighting.removeStyles();
                    if (!openedFile.isModifiable()) {
                        my_editor.setOption("readOnly", true);
                    } else {
                        my_editor.setOption("readOnly", false);
                    }
                    my_editor.focus();
                    my_editor.setValue(openedFile.getText());
                    openedFile.getChangesHistory() != null ? my_editor.setHistory(openedFile.getChangesHistory()) : my_editor.clearHistory();
                    highlighting.updateHighlighting();
                } else {
                    throw("Previous file wasn't closed");
                }
            },
            closeFile: function () {
                if (openedFile != null) {
                    openedFile.setChangesHistory(my_editor.getHistory());
                    openedFile = null;
                }
                highlighting.removeStyles();
                my_editor.clearHistory();
                my_editor.setValue("");
                document.getElementById("workspace-overlay").style.display = "block";
            },
            reloadFile: function () {
                if (openedFile != null) {
                    my_editor.focus();
                    my_editor.setValue(openedFile.getText());
                    openedFile.getChangesHistory() != null ? my_editor.setHistory(openedFile.getChangesHistory()) : my_editor.clearHistory();
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
            }
        };

        my_editor = CodeMirror.fromTextArea(document.getElementById("code"), {
            lineNumbers: true,
            styleActiveLine: true,
            matchBrackets: true,
            mode: "text/kotlin",
            autoCloseBrackets: true,
            continueComments: true,
            extraKeys: {
                "Ctrl-Space": function () {
                    completionProvider.getCompletion(accordion.getSelectedProject(), openedFile.getName(),
                        my_editor.getCursor(true).line, my_editor.getCursor(true).ch);
                },
                "Shift-Tab": false,
                "Ctrl-Alt-L": "indentAuto",
                "Ctrl-/": "toggleComment"
            },
            gutters: ["errors-and-warnings-gutter"],
            tabSize: 4
        });

        my_editor.on("change", function () {
            if (openedFile != null) {
                openedFile.setText(my_editor.getValue());
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