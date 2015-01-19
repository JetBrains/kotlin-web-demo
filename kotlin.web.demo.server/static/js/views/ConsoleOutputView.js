/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
 * Created by Semyon.Atamas on 11/24/2014.
 */


var ConsoleOutputView = (function () {
    function ConsoleOutputView() {
        var currentLine;
        var Error = (function () {
            var instance = {
                print: function (text) {
                    var span = document.createElement("span");
                    span.className = "error-output";
                    span.textContent = unEscapeString(text).replace(RegExp("</br>", "g"), "\n");
                    currentLine.appendChild(span);
                },
                println: function (text) {
                    instance.print(text);
                    newLine();
                },
                addReference: function (referenceElement) {
                    currentLine.appendChild(referenceElement);
                }
            };

            function newLine() {
                if (currentLine != null) {
                    currentLine.className = "error-output";
                }
                currentLine = document.createElement("div");
                element.appendChild(currentLine);
            }

            return instance
        })();

        var Out = (function () {
            var instance = {
                print: function (text) {
                    var span = document.createElement("span");
                    span.className = "standard-output";
                    span.textContent = unEscapeString(text).replace(RegExp("</br>", "g"), "\n");
                    currentLine.appendChild(span);
                },
                println: function (text) {
                    instance.print(text);
                    newLine();
                },
                addReference: function (referenceElement) {
                    currentLine.appendChild(referenceElement);
                }
            };

            function newLine() {
                if (currentLine != null) {
                    currentLine.className = "standard-output";
                }
                currentLine = document.createElement("div");
                element.appendChild(currentLine);
            }

            return instance
        })();

        var instance = {
            clear: function () {
                element.innerHTML = "";
                currentLine = document.createElement("div");
                element.appendChild(currentLine);
            },
            printException: function (exception) {
                Error.println("");
                if(exception.message != null) {
                    Error.println("Exception in thread \"main\" " + exception.fullName + ': ' + unEscapeString(exception.message) + '\n');
                } else{
                    Error.println("Exception in thread \"main\" " + exception.fullName + '\n');
                }
                instance.printExceptionBody(exception);
            },
            printExceptionBody: function (exception) {
                printStackTrace(exception.stackTrace);
                printExceptionCause(exception);
                Error.println("");
            },
            addElement: function (element) {
                currentLine.appendChild(element);
            },
            makeReference: function (fileName, lineNo) {
                var a = document.createElement("a");
                a.innerHTML = fileName + ':' + lineNo;
                a.href = "#";
                return a;
            },
            writeTo: function (_element) {
                element = _element;
                currentLine = document.createElement("div");
                element.appendChild(currentLine);
            },
            printMarkedTextToConsole: function (text) {
                text = unEscapeString(text);
                text = text.replace(/(\r\n|\n|\r)/g, "</br>");
                var outStreamMessages = getOutputStreamMessagesFromMarkedText(text);
                for (var j = 0; j < outStreamMessages.length; ++j) {
                    consoleOutputView.out.print(outStreamMessages[j]);
                }

                var errorStreamMessages = getErrorStreamMessagesFromMarkedText(text);
                for (var j = 0; j < errorStreamMessages.length; ++j) {
                    consoleOutputView.err.print(errorStreamMessages[j]);
                }
            },
            err: Error,
            out: Out
        };

        var element = document.createElement("div");

        function printStackTrace(stackTrace){
            for (var i = 0; i < stackTrace.length; ++i) {
                if (stackTrace[i].className.startsWith("sun.reflect")) {
                    break;
                }
                Error.print('    at ' + stackTrace[i].className + '(');
                Error.addReference(instance.makeReference(stackTrace[i].fileName, stackTrace[i].lineNumber));
                Error.println(')');
            }
        }

        function printExceptionCause(exception){
            if(exception.cause != null) {
                var cause = exception.cause;
                if(cause.message != null) {
                    Error.println("Caused by: " + cause.fullName + ': ' + unEscapeString(cause.message) + '\n');
                } else{
                    Error.println("Caused by: " + cause.fullName + '\n');
                }
                printStackTrace(cause.stackTrace);
                printExceptionCause(cause);
            }
        }

        function getOutputStreamMessagesFromMarkedText(text) {
            return getAllRegexpMatches(text, new RegExp("<outStream>(.*?)</outStream>", "g"));
        }

        function getErrorStreamMessagesFromMarkedText(text) {
            return getAllRegexpMatches(text, new RegExp("<errStream>(.*?)</errStream>", "g"));
        }

        function getAllRegexpMatches(text, regexp) {
            var result = [];
            var match = regexp.exec(text);
            while (match != null) {
                result.push(match[1]);
                match = regexp.exec(text);
            }
            return result;
        }

        return instance;
    }

    return ConsoleOutputView
})();