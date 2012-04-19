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
        var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.type.JAVA);

        var completionProvider;
        var highlightingProvider;

        var CompletionObject = (function () {
            var keywords;
            var isContinueComplete = false;


            function CompletionObject() {
                $("body div:first").after("<div style=\"display: none;\" class=\"completions completionPopUpWindow\"></div>");
                var instance = {
                    processCompletionResult:function (data) {
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
                            my_editor.replaceRange(str, {line:cur.line, ch:token.end}, {line:cur.line, ch:token.end});
                        } else {
                            my_editor.replaceRange(str, {line:cur.line, ch:token.start}, {line:cur.line, ch:token.end});
                        }
                    }
                }

                var complete = $("div.completionPopUpWindow");

                complete.html("");
                complete.css("display", "block");

                var sel = document.createElement("select");
                sel.id = "selectId";
                var i = 0;
                for (i = 0; i < completions.length; ++i) {
                    var opt = document.createElement("option");
                    var pEl = document.createElement("p");
                    pEl.className = "lookupElement";

                    var icon = document.createElement("img");
                    icon.className = "lookupElementIcon";
                    if (completions[i].icon != "") {
                        icon.src = "/static/icons/" + completions[i].icon + ".png";
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
                    sel.appendChild(opt);
                }

                complete.append(sel);


                sel.multiple = true;

                if (data == COMPLETION_ISNOT_AVAILABLE) {
                    sel.firstChild.selected = false;
                }
                sel.size = Math.min(10, i);

                var pos = my_editor.cursorCoords();
                complete.css("left", pos.x + "px");
                complete.css("top", pos.yBot + "px");
                complete.focus();

                // Hack to hide the scrollbar.
                if (i <= 10) {
                    complete.css("width", (sel.clientWidth - 1) + "px");
                    complete.css("height", (sel.size * 18) + "px");
                }   else {
                    complete.css("width", "auto");
                    complete.css("height", "auto");
                }


                var done = false;

                function close() {
                    if (done) return;
                    done = true;
                    complete.css("display", "none");
                    complete.html("");
                    //complete.parentNode.removeChild(complete);
                }

                function pick() {
                    var text = sel.options[sel.selectedIndex].childNodes[0].childNodes[1].textContent;
                    if (typeof text == undefined) {
                        text = sel.options[sel.selectedIndex].childNodes[0].childNodes[1].innerHTML;
                    }
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
                }

                connect(sel, "blur", close);
                connect(sel, "keydown", function (event) {
                    var code = event.keyCode;

                    // Enter and space
                    if (code == 13 || code == 32) {
                        isContinueComplete = false;
                        event.stop();
                        pick();
                    }
                    // Escape
                    else if (code == 27) {
                        isContinueComplete = false;
                        event.stop();
                        close();
                        my_editor.focus();
                    } else if (code == 8) {
                        event.stop();
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


                connect(sel, "dblclick", pick);

                sel.focus();
                // Opera sometimes ignores focusing a freshly created node
                if (window.opera) setTimeout(function () {
                    if (!done) sel.focus();
                }, 100);
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
            var arrayClasses = [];
            var arrayLinesMarkers = [];

            function HighlightingObject() {
                var instance = {
                    processHighlightingResult:function (data) {
                        process(data);
                    }
                };

                return instance;
            }

            function removeStyles() {
                var i = 0;
                while (arrayClasses[i] != undefined) {
                    arrayClasses[i].clear();
                    i++;
                }
                i = 0;
                while (arrayLinesMarkers[i] != undefined) {
                    my_editor.clearMarker(arrayLinesMarkers[i]);
                    i++;
                }
            }

            function process(data) {
                removeStyles();
                arrayClasses = [];
                arrayLinesMarkers = [];

                if ((data[0] != undefined) && (data[0].exception != undefined)) {
                    //todo eventHandler.fire("write_exception", data);
                    return;
                }

                var i = 0;

                function processError(i, f) {
                    if (data[i] == undefined) {
                        return;
                    }
                    arrayClasses.push(my_editor.markText(eval('(' + data[i].x + ')'), eval('(' + data[i].y + ')'), data[i].className));

                    var title = unEscapeString(data[i].titleName);
                    var start = eval('(' + data[i].x + ')');
                    var severity = data[i].severity;

                    if ((my_editor.lineInfo(start.line) != null) && (my_editor.lineInfo(start.line).markerText == null) || (my_editor.lineInfo(start.line) == null)) {
                        my_editor.setMarker(start.line, '<span class=\"' + severity + 'gutter\" title="' + title + '">  </span>%N%');
                        arrayLinesMarkers.push(start.line);
                    } else {
                        var text = my_editor.lineInfo(start.line).markerText;

                        var resultSpan = "";
                        if ((severity == "WARNING") && text.indexOf("ERRORgutter") != -1) {
                            text = text.substring(text.indexOf("title=\"") + 7);
                            text = text.substring(0, text.indexOf("\""));
//                    resultSpan += title + "\n ---next error--- \n" + text.substring(pos);
                            resultSpan = '<span class=\"ERRORgutter\" title="' + text + "\n ---next error--- \n" + title + '">  </span>%N%';
                        } else {
                            text = text.substring(text.indexOf("title=\"") + 7);
                            text = text.substring(0, text.indexOf("\""));
                            resultSpan = '<span class=\"' + severity + 'gutter\" title="' + text + "\n ---next error--- \n" + title + '">  </span>%N%';
                        }
                        //this.arrayLinesMarkers.pop();
                        my_editor.setMarker(start.line, resultSpan);
                        arrayLinesMarkers.push(start.line);
                    }

                    var el = document.getElementById(start.line + "_" + start.ch);
                    if (el != null) {
                        el.setAttribute("title", title);
                    }

                    i++;
                    setTimeout(function (i) {
                        return function () {
                            f(i, processError);
                        }
                    }(i), 10);
                }

                processError(i, processError);
            }

            return HighlightingObject;
        })();

        var completion = new CompletionObject();
        var highlighting = new HighlightingObject();


        my_editor = CodeMirror.fromTextArea(document.getElementById("code"), {
            lineNumbers:true,
            matchBrackets:true,
            mode:"text/kotlin",
            extraKeys:{
                "Ctrl-Space":function () {
                    completionProvider.getCompletion(configuration.type, my_editor.getValue(),
                        my_editor.getCursor(true).line, my_editor.getCursor(true).ch);
                }

            },
            onChange:runTimerForNonPrinting,
            onCursorActivity:function () {
                instance.onCursorActivity(my_editor.getCursor());
            },
            minHeight:"430px",
            tabSize:2
        });

        var instance = {
            loadExampleOrProgram:function (status, example) {
                if (status) {
                    my_editor.setValue(example.text);
                    isEditorContentChanged = false;
                }
            },
            showCompletionResult:function (data) {
                completion.processCompletionResult(data);
            },
            addMarkers:function (data) {
                highlighting.processHighlightingResult(data);
            },
            setConfiguration:function (conf) {
                configuration = conf;
            },
            setHighlighterDecorator:function (decorator) {
                highlightingProvider = decorator;
            },
            setCompletionDecorator:function (decorator) {
                completionProvider = decorator;
            },
            getProgramText:function () {
                return my_editor.getValue();
            },
            isEditorContentChanged:function () {
                return isEditorContentChanged;
            },
            markAsUnchanged:function () {
                isEditorContentChanged = false;
            },
            indentAll:function () {
                my_editor.setSelection({line:0, ch:0}, {line:my_editor.lineCount() - 1, ch:0});
                my_editor.indentSelection("smart");
            },
            refreshMode:function () {
                my_editor.setOption("mode", "kotlin");
            },
            setText:function (text) {
                my_editor.focus();
                my_editor.setValue(text);
                isEditorContentChanged = false;
            },
            clearMarkers:function () {
                for (var i = 0; i < my_editor.lineCount(); i++) {
                    try {
                        my_editor.clearMarker(i);
                    } catch (e) {
                        //Absent marker for line
                    }
                }
            },
            onCursorActivity:function (cursorPosition) {
            },
            getWordAtCursor:function (cursorPosition) {
                var word = my_editor.getTokenAt(cursorPosition).string;
                if (checkDataForNull(word)) {
                    return word;
                }
                return "";
            },
            getMessageForLineAtCursor:function (cursorPosition) {
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
        };

        var timer;
        var timerIntervalForNonPrinting = 300;
        var isEditorContentChanged = false;

        function runTimerForNonPrinting() {
            isEditorContentChanged = true;
            if (timer) {
                clearTimeout(timer);
                timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
            }
            else {
                timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
            }
        }

        function getHighlighting() {
            if (configuration.mode.name != Configuration.mode.ONRUN.name) {
                highlightingProvider.getHighlighting(
                    configuration.type,
                    my_editor.getValue(),
                    instance.addMarkers
                );
            }

        }

        return instance;
    }


    return KotlinEditor;
})();