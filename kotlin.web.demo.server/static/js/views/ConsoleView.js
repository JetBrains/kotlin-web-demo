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

var ConsoleView = (function () {

    var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.type.JAVA);

    var console;

    function ConsoleView(element, /*Nullable*/ tabs) {

        var JAVASCRIPT_CODE = "Generated JavaScript code";
        console = element;

        var instance = {
            setOutput: function (data) {
                setOutput(data);
            },
            setConfiguration: function (conf) {
                configuration = conf;
            },
            writeException: function (data) {
                writeException(data);
            }
        };

        if (tabs != null) {
            tabs.tabs();
        }

        function writeException(data) {
            if (data != undefined && data[0] != undefined && data[0].exception != undefined) {
                element.html("");
                if (tabs != null) {
                    tabs.tabs("option", "active", 1);
                }
                var i = 0;
                var output = [];
                while (data[i] != undefined) {
                    output.push({"text": data[i].exception, "type": data[i].type});
                    i++;
                }
                setOutput(output);

            } else if (data == undefined || data == null) {
            } else {
                element.html("");
                if (tabs != null) {
                    tabs.tabs('option', 'active' , 1);
                }
                var output = [
                    {"text": data, "type": "err"}
                ];
                setOutput(output);
            }
        }

        function makeCodeReference(file, lineNo) {
            var a = document.createElement("a");
            a.href = "#code";
            a.innerHTML = "(" + file + ":" + lineNo + ")";
            var id = "stack_trace_code_reference_" + lineNo;
            a.id = id;
            $(document).on("click", "#" + id, function () {
                editor.setCursor(lineNo - 1, 0);
                editor.focus();
            });
            return a;
        }

        function createStdErrorForConsoleView(element, template, lines) {
            var i = 0;
            var pos = template.indexOf("%STACK_TRACE_LINE%");
            while (pos > -1) {
                var s = document.createElement("span");
                s.innerHTML = template.substr(0, pos) + "\tat " + lines[i].function;
                template = template.substr(pos + "%STACK_TRACE_LINE%".length);
                element.appendChild(s);
                element.appendChild(makeCodeReference(lines[i].file, lines[i].lineNo));
                pos = template.indexOf("%STACK_TRACE_LINE%");
                ++i;
            }
        }

        function setOutput(data) {
            generatedCodeView.clean();
            element.html("");
            if (tabs != null) {
                tabs.tabs("option", "active", 1);
            }

            if (data != null) {
                if (tabs != null) {
                    tabs.tabs("option", "active", 1);
                }
                var i = 0;
                while (data[i] != undefined) {
                    if (data[i].type == "toggle-info") {
                        generatedCodeView.setOutput(data[i]);
                    } else {
                        var p = document.createElement("p");
                        var message = data[i].text;

                        if ((data[i].type == "err") && (message != "")) {
                            p.className = "consoleViewError";
                            if (message == "") {
                                p.innerHTML = "Unknown exception."
                            } else if (message == "timeout : timeout") {
                                p.innerHTML = "Server didn't response for 10 seconds."
                            } else {
                                p.innerHTML = unEscapeString(message);
//                                var stdErr = JSON.parse(message);
//                                createStdErrorForConsoleView(p, stdErr.stdErr, stdErr.stackTraceLines)
                            }
                        } else if (data[i].type == "info") {
                            generatedCodeView.setOutput(data[i]);
//                            p.className = "consoleViewInfo";
//                            p.innerHTML = unEscapeString(message);
                        } else {
                            p.innerHTML = unEscapeString(message);
                        }


                    }

                    element.append(p);
                    i++;
                }
            }
        }

        return instance;
    }


    /*ConsoleView.showJsCode = function () {
     $(":nth-child(2)", console).remove();
     $(":first-child", console).after("<p class='consoleViewInfo'>" + generatedJsCode + "</p>");
     };*/

    ConsoleView.toggleElement = function (name) {
        $("div." + name).toggle();
    };


    return ConsoleView;
})();