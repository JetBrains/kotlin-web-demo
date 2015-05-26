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

import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.text.js.RegExp

class ConsoleView(
        private val element: HTMLDivElement,
        private val tabs: dynamic
) {
    fun clear() {
        element.innerHTML = ""
    }

    fun writeException(data: dynamic) {
        prepareTab();
        if (data != undefined && data[0] != undefined && data[0].exception != undefined) {
            var output = arrayListOf<dynamic>();
            for (exception in data) {
                exception.stackTrace = (exception.stackTrace ?: "");
                output.add(json(
                        "text" to exception.exception.replace(RegExp("<br/>", "g"), "\n"),
                        "stackTrace" to exception.stackTrace.replace(RegExp("<br/>", "g"), "\n"),
                        "type" to exception.type
                ))
            }
            output.forEach { element ->
                setOutput(element)
            };
        } else if (data == undefined || data == null) {
        } else {
            if (data == "") {
                consoleOutputView.err.println("Unknown exception.");
            } else if (data == "timeout : timeout") {
                consoleOutputView.err.println("Server didn't respond for 10 seconds.");
            } else {
                consoleOutputView.err.println(data)
            }
        }
    }

    fun setOutput(data: dynamic) {
        prepareTab()
        if (data.type == "jsException") {
            if (data.exception.stack != null && data.exception.stack != "") {
                consoleOutputView.err.println(data.exception.stack);
            } else {
                consoleOutputView.err.println("Unknown error");
            }
        } else if (data.type == "out") {
            consoleOutputView.printMarkedTextToConsole(data.text);
            if (data.exception != null) {
                consoleOutputView.printException(data.exception);
            }
        } else if (data.type == "jsOut") {
            consoleOutputView.out.print(data.text);
        } else if (data.type == "err") {
            var message = data.text;
            if (message == "") {
                consoleOutputView.err.println("Unknown exception.");
            } else if (message == "timeout : timeout") {
                consoleOutputView.err.println("Server didn't respond for 10 seconds.");
            } else {
                consoleOutputView.err.println(message);
                if (data.stackTrace != null) {
                    consoleOutputView.out.println(data.stackTrace);
                }
            }
        } else {
            throw Exception("Unknown data type");
        }
    }

    private fun prepareTab() {
        element.innerHTML = "";
        var consoleOutputElement = document.createElement("div");
        element.appendChild(consoleOutputElement);
        consoleOutputView.writeTo(consoleOutputElement);
        consoleOutputElement.className = "consoleOutput";
        tabs?.tabs("option", "active", 1)
    }
}