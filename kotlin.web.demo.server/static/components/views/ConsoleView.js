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
            setOutput:function (data) {
                updateConsole(data);
            },
            setConfiguration:function (conf) {
                configuration = conf;
            },
            writeException:function (data) {
                writeException(data);
            }
        };

        if (tabs != null) {
            tabs.tabs();
        }

        function writeException(data) {
            if (typeof data != "undefined" && typeof data[0] != "undefined" && typeof data[0].exception != "undefined") {
                element.html("");
                if (tabs != null) {
                    tabs.tabs("select", 1);
                }
                var i = 0;
                while (typeof data[i] != "undefined") {
                    createException(data[i]);
                    i++;
                }
            } else if (data == undefined || data == null) {
            } else {
                element.html("");
                if (tabs != null) {
                    tabs.tabs("select", 1);
                }
                element.html(createRedElement("EXCEPTION: " + data));
            }
        }

        function createException(ex) {
            var console = document.createElement("div");
            if (ex.type == "out") {
                element.append(createElementForConsole("STACKTRACE", null, unEscapeString(ex.exception)));
            } else {
                element.append(createElementForConsole("ERROR", null, unEscapeString(ex.exception)));
            }
        }

        function createElementForConsole(severity, start, title) {
            var p = document.createElement("p");
            if (severity == 'WARNING') {
                if (title.indexOf("is never used") > 0) {
                    p.className = "problemsViewWarningNeverUsed";
                } else {
                    p.className = "problemsViewWarning";
                }

            } else if (severity == 'STACKTRACE') {
                p.className = "problemsViewStacktrace";
            } else {
                p.className = "problemsViewError";
            }
            var titleDiv = document.createElement("span");
            if (start == null) {
                titleDiv.innerHTML = " " + unEscapeString(title);
            } else {
                titleDiv.innerHTML = "(" + (start.line + 1) + ", " + (start.ch + 1) + ") : " + unEscapeString(title);
            }
            p.appendChild(titleDiv);
            return p;
        }

        function setConsoleMessage(message) {
            element.html(message);
        }

        function updateConsole(data) {
            element.html("");
            if (tabs != null) {
                tabs.tabs("select", 1);
            }

            if (data != null) {
                if (tabs != null) {
                    tabs.tabs("select", 1);
                }
                var i = 0;
                var errors = document.createElement("div");
                while (typeof data[i] != "undefined") {
                    var p = document.createElement("p");
                    if (data[i].type == "toggle-info") {
                        p.appendChild(createToggleElement(data[i]));
                        p.className = "consoleViewInfo";
                    } else {
                        if ((data[i].type == "err") && (data[i].text != "")) {
                            p.className = "consoleViewError";
                        }
                        if (data[i].type == "info") {
                            p.className = "consoleViewInfo";
                        }
                        p.innerHTML = unEscapeString(data[i].text);
                    }

                    errors.appendChild(p);
                    i++;
                }
                element.append(errors);
            }
        }

        function createToggleElement(data) {
            var mainDiv = document.createElement("div");
            var toggleDiv = document.createElement("div");
            var elementName = "toggledDivForJsCode" + random();
            toggleDiv.className = elementName;
            toggleDiv.style.marginTop = "5px";
            toggleDiv.style.display = "none";
            toggleDiv.innerHTML = safe_tags_replace(data.text);
            var divLink = document.createElement("div");
            divLink.innerHTML = "<a href='javascript:void(0);' onclick='ConsoleView.toggleElement(\"" + elementName + "\");'>"
                + JAVASCRIPT_CODE + "</a>";

            mainDiv.appendChild(divLink);
            mainDiv.appendChild(toggleDiv);
            return mainDiv;
        }

        function createRedElement(text) {
            var div = document.createElement("div");
            var p = document.createElement("p");
            p.className = "consoleViewError";
            p.innerHTML = text;
            div.appendChild(p);
            return div.innerHTML;
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