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
                setOutput(data);
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
            if (data != undefined && data[0] != undefined && data[0].exception != undefined) {
                element.html("");
                if (tabs != null) {
                    tabs.tabs("select", 1);
                }
                var i = 0;
                var output = [];
                while (data[i] != undefined) {
                    output.push({"text":data[i].exception, "type":data[i].type});
                    i++;
                }
                setOutput(output);

            } else if (data == undefined || data == null) {
            } else {
                element.html("");
                if (tabs != null) {
                    tabs.tabs("select", 1);
                }
                var output = [
                    {"text":data, "type":"err"}
                ];
                setOutput(output);
            }
        }

        function setOutput(data) {
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
                while (data[i] != undefined) {
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
                        var message = data[i].text;
                        if (data[i].type == "err" && message == "") {
                            message = "Unknown exception."
                        } else if (message == "timeout : timeout") {
                            message = "Server didn't response for 10 seconds."
                        }
                        p.innerHTML = unEscapeString(message);
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