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
    function ConsoleView(element, /*Nullable*/ tabs) {

        var JAVASCRIPT_CODE = "Generated JavaScript code";

        var instance = {
            setOutput: function (data) {
                consoleOutputView.appendTo(document.body);
                element.html("");
                consoleOutputView.appendTo(element);

                setOutput(console, data);
            },
            writeException: function (data) {
                element.html("");
                var scroll = document.createElement("div");
                scroll.className = "scroll";
                element.append(scroll);

                var console = document.createElement("div");
                console.className = "result-view";
                scroll.appendChild(console);
                writeException(console, data);
            }
        };

        if (tabs != null) {
            tabs.tabs();
        }

        function writeException(console, data) {
            if (data != undefined && data[0] != undefined && data[0].exception != undefined) {
                console.innerHTML = "";
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
                console.innerHTML = "";
                if (tabs != null) {
                    tabs.tabs('option', 'active', 1);
                }
                var output = [
                    {"text": data, "type": "err"}
                ];
                setOutput(output);
            }
        }

        var i = 0;

        function makeCodeReference(lineNo) {
            var a = document.createElement("a");
            a.ref = "#code";
            a.innerHTML = "dummy.kt:" + lineNo;
            a.id = "code_reference_" + i;
            $(document).on("click", "#code_reference_" + i, function () {
                editor.setCursor(parseInt(lineNo - 1, 0));
                editor.focus();
            });
            i = i + 1;
            return a;
        }

        function createStdErrorForConsoleView(element, message) {
            var regExp = /(.*?)\(dummy\.kt:([1-9]+)\)/;
            var ref = regExp.exec(message);
            if (regExp == null) {
                while (ref != null) {

                    var span = document.createElement("span");
                    span.innerHTML = ref[1] + "(";
                    element.appendChild(span);


                    element.appendChild(makeCodeReference(ref[2]));

                    span = document.createElement("span");
                    span.innerHTML = ")";
                    element.appendChild(span);

                    message = message.substr(ref[0].length);
                    ref = regExp.exec(message);
                }
            } else {
                element.innerHTML = message;
            }
        }

        function setOutput(console, data) {
            generatedCodeView.clean();
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
//                                p.innerHTML = unEscapeString(message);
                                createStdErrorForConsoleView(p, message)
                            }
                        } else if (data[i].type == "info") {
                            generatedCodeView.setOutput(data[i]);
                        } else {
                            p.innerHTML = unEscapeString(message);
                        }


                    }

                    console.appendChild(p);
                    i++;
                }
            }
        }

        return instance;
    }


    ConsoleView.toggleElement = function (name) {
        $("div." + name).toggle();
    };


    return ConsoleView;
})();