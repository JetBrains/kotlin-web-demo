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

package views.tabs

import kotlinx.html.*
import kotlinx.html.js.*
import kotlinx.html.dom.*
import org.w3c.dom.HTMLDivElement
import providers.JavaRunResult
import views.tabs.OutputView
import kotlin.text.js.RegExp

class ConsoleView(
        private val element: HTMLDivElement,
        private val tabs: dynamic
) {
    fun clear() {
        element.innerHTML = ""
    }

    fun writeException(data: dynamic) {
        val outputView = prepareTab()
        if (data != undefined && data[0] != undefined && data[0].exception != undefined) {
            var output = arrayListOf<dynamic>()
            for (exception in data) {
                exception.stackTrace = (exception.stackTrace ?: "")
                output.add(json(
                        "text" to exception.exception.replace(RegExp("<br/>", "g"), "\n"),
                        "stackTrace" to exception.stackTrace.replace(RegExp("<br/>", "g"), "\n"),
                        "type" to exception.type
                ))
            }
            output.forEach { element ->
                setOutput(element)
            }
        } else if (data == undefined || data == null) {
        } else {
            if (data == "") {
                outputView.printErrorLine("Unknown exception.")
            } else if (data == "timeout : timeout") {
                outputView.printErrorLine("Server didn't respond for 10 seconds.")
            } else {
                outputView.printErrorLine(data)
            }
        }
    }

    fun showJavaRunResult(runResult: JavaRunResult) {
        val outputView = prepareTab()
        outputView.printMarkedText(runResult.text ?: "")
        if (runResult.exception != null) {
            outputView.printException(runResult.exception)
        }
    }

    fun setOutput(data: dynamic) {
        val outputView = prepareTab()
        if (data.type == "jsException") {
            if (data.exception.stack != null && data.exception.stack != "") {
                outputView.printErrorLine(data.exception.stack)
            } else {
                outputView.printErrorLine("Unknown error")
            }
        } else if (data.type == "out") {
            outputView.printMarkedText(data.text)
            if (data.exception != null) {
                outputView.printException(data.exception)
            }
        } else if (data.type == "jsOut") {
            outputView.print(data.text)
        } else if (data.type == "err") {
            var message = data.text
            if (message == "") {
                outputView.printErrorLine("Unknown exception.")
            } else if (message == "timeout : timeout") {
                outputView.printErrorLine("Server didn't respond for 10 seconds.")
            } else {
                outputView.printErrorLine(message)
                if (data.stackTrace != null) {
                    outputView.println(data.stackTrace)
                }
            }
        } else {
            throw Exception("Unknown data type")
        }
    }

    fun showJsException(exception: Throwable) {
        val outputView = prepareTab()
        outputView.printErrorLine("Unhandled JavaScript exception")
    }

    fun showUnmarkedText(text: String) {
        val outputView = prepareTab()
        outputView.print(text)
    }

    private fun prepareTab(): OutputView {
        element.innerHTML = ""
        return OutputView(element.append.div {
            classes = setOf("consoleOutput")
        })
    }
}