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

        var instance = {
            setOutput: function (data) {
                prepareTab();
                setOutput(data);
            },
            writeException: function (data) {
                prepareTab();
                if (data != undefined && data[0] != undefined && data[0].exception != undefined) {
                    var i = 0;
                    var output = [];
                    while (data[i] != undefined) {
                        output.push({"text": data[i].exception, "type": data[i].type});
                        i++;
                    }
                    setOutput(output);
                } else if (data == undefined || data == null) {
                } else {
                    if (data == "") {
                        consoleOutputView.printToError(["Unknown exception."]);
                    } else if (data == "timeout : timeout") {
                        consoleOutputView.printToError(["Server didn't response for 10 seconds."]);
                    }
                }
            }
        };

        if (tabs != null) {
            tabs.tabs();
        }

        function prepareTab(){
            element.innerHTML = "";
            var consoleOutputElement = document.createElement("div");
            element.appendChild(consoleOutputElement);
            consoleOutputView.writeTo(consoleOutputElement);
            if (tabs != null) {
                tabs.tabs("option", "active", 1);
            }
        }

        function setOutput(data) {
            for(var i = 0 ;  i < data.length; ++i){
                if(data[i].type == "out"){
                    consoleOutputView.print(unEscapeString(data[i].text));
                    if(data[i].exception != null){
                        consoleOutputView.printException(data[i].exception);
                    }
                } else if (data[i].type == "toggle-info" || data[i].type == "info") {
                    generatedCodeView.setOutput(data[i]);
                } else if(data[i].type == "err"){
                    var message = data[i].text;
                    if (message == "") {
                        consoleOutputView.printToError(["Unknown exception."]);
                    } else if (message == "timeout : timeout") {
                        consoleOutputView.printToError("Server didn't response for 10 seconds.");
                    }
                }
            }
        }

        return instance;
    }

    return ConsoleView;
})();