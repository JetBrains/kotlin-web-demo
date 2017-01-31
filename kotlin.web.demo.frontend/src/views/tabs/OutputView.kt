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

import application.makeReference
import kotlinx.html.dom.append
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import utils.unEscapeString
import kotlin.browser.document


class OutputView(val element: HTMLElement) {

    public fun print(s: String) {
        element.append.span {
            +s.replace("</br>", "\n").replace("<br/>", "\n")
            classes = setOf("standard-output")
        }
    }

    public fun println(s: String) {
        print(s + '\n')
    }

    public fun printError(s: String): HTMLSpanElement = element.append.span {
            +s.replace("</br>", "\n").replace("<br/>", "\n")
            classes = setOf("error-output")
    }

    public fun printErrorLine(s: String = "") {
        printError(s + '\n')
    }

    fun printMarkedText(text: String) {
        var unprocessedFragment = unEscapeString(text)
        while (unprocessedFragment.isNotBlank()){
            val spanElement = document.createElement("span") as HTMLSpanElement
            if(unprocessedFragment.startsWith("<outStream>")){
                unprocessedFragment = unprocessedFragment.substringAfter("<outStream>")
                spanElement.className = "standard-output"
                spanElement.textContent = unprocessedFragment.substringBefore("</outStream>")
                unprocessedFragment = unprocessedFragment.substringAfter("</outStream>")
            } else{
                unprocessedFragment = unprocessedFragment.substringAfter("<errStream>")
                spanElement.className = "error-output"
                spanElement.textContent = unprocessedFragment.substringBefore("</errStream>")
                unprocessedFragment = unprocessedFragment.substringAfter("</errStream>")
            }
            element.appendChild(spanElement)
        }
    }

    fun printException(exception: dynamic) {
        printErrorLine()
        printErrorLine(
                if (exception.message != null) {
                    "Exception in thread \"main\" " + exception.fullName + ": " + unEscapeString(exception.message)
                } else {
                    "Exception in thread \"main\" " + exception.fullName
                }
        )
        printExceptionBody(exception)
    }

    fun printExceptionBody(exception: dynamic) {
        printStackTrace(exception.stackTrace)
        printExceptionCause(exception)
        printErrorLine()
    }

    private fun printStackTrace(stackTrace: Array<dynamic>){
        for (stackTraceElement in stackTrace) {
            if (stackTraceElement.className.startsWith("sun.reflect")) {
                break
            }
            val element = printError("    at " + stackTraceElement.className + '.' + stackTraceElement.methodName + '(')
            element.appendChild(makeReference(stackTraceElement.fileName, stackTraceElement.lineNumber))
            printErrorLine(")")
        }
    }

    private fun printExceptionCause(exception: dynamic){
        if(exception.cause != null) {
            var cause = exception.cause
            if(cause.message != null) {
                printErrorLine("Caused by: " + cause.fullName + ": " + unEscapeString(cause.message) + '\n')
            } else{
                printErrorLine("Caused by: " + cause.fullName + '\n')
            }
            printStackTrace(cause.stackTrace)
            printExceptionCause(cause)
        }
    }
}