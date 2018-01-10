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

package views.dialogs

import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.title
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.KeyCode
import utils.jquery.jq
import utils.jquery.ui.Dialog
import kotlin.browser.document
import kotlin.dom.addClass


external object difflib {
    class SequenceMatcher(baseTextLines: Array<String>, newTextLines: Array<String>){
        fun get_opcodes(): Array<dynamic> = definedExternally
    }
}

object DifferenceDialog{
    fun open(baseText: String, newText: String) {
        leftLineElements.clear()
        rightLineElements.clear()
        val baseTextLines = baseText.split("</br>")
        val newTextLines = newText.split("</br>")
        val sequenceMatcher = difflib.SequenceMatcher(baseTextLines.toTypedArray(), newTextLines.toTypedArray())
        createDialogContent(baseTextLines, newTextLines, sequenceMatcher.get_opcodes())
        jq(dialogElement).dialog("open")
        jq(differenceElement).height(jq(dialogElement).height().toInt() - jq(colorsHelp).outerHeight(true).toInt())
    }

    val leftLineElements = arrayListOf<HTMLElement>()
    val rightLineElements = arrayListOf<HTMLElement>()

    val dialogElement = document.body!!.append.div {
        classes = setOf("difference-dialog")
        title = "Comparison failure"
    }

    val differenceElement = dialogElement.append.div {}

    val colorsHelp = dialogElement.append.div{
        classes = setOf("colors-help")
    }

    val dialog = Dialog(
            dialogElement,
            minWidth = 700,
            resizable = false,
            width = 700,
            minHeight = 700,
            autoOpen = false,
            modal = true
    )

    init {
        createColorHelp("delete")
        createColorHelp("replace")
        createColorHelp("insert")

        jq(dialogElement).keydown({ event ->
            if (event.keyCode == KeyCode.ENTER.code) {
                dialog.close()
            }
            event.stopPropagation()
        })
    }

    private fun createDialogContent(expectedLines: List<String>, actualLines: List<String>, opCodes: Array<dynamic>) {
        differenceElement.innerHTML = ""
        differenceElement.className = "difference-dialog-content"
        differenceElement.appendChild(createDifferenceElement(expectedLines, opCodes, false))
        differenceElement.appendChild(createDifferenceElement(actualLines, opCodes, true))
    }

    private fun createDifferenceElement(lines: List<String>, opCodes: Array<dynamic>, isRightElement: Boolean): HTMLDivElement {
        val element = document.createElement("div") as HTMLDivElement
        element.className = "diff"
        val glutter = createGlutterElement()
        element.appendChild(glutter)

        val outputElement = document.createElement("div")
        outputElement.className = "diff-output"
        element.appendChild(outputElement)

        val lineElements = if (isRightElement) rightLineElements else leftLineElements
        for (i in 0 until lines.size) {
            val lineNumber = document.createElement("div")
            lineNumber.className = "diff-lineNumber"
            lineNumber.innerHTML = i.toString() + ""
            glutter.appendChild(lineNumber)

            val line = document.createElement("div") as HTMLDivElement
            line.className = "diff-line"
            line.innerHTML = lines[i]
            outputElement.appendChild(line)

            lineElements.add(line)
        }

        for (i in 0 until opCodes.size) {
            val code = opCodes[i]
            val change = code[0]
            val b = (code[1] as Number).toInt()
            val be = (code[2] as Number).toInt()
            val n = (code[3] as Number).toInt()
            val ne = (code[4] as Number).toInt()

            if (!isRightElement) {
                for (j in b until be) {
                    lineElements[j].addClass(change + "-color")
                }
            } else {
                for (j in n until ne) {
                    lineElements[j].addClass(change + "-color")
                }
            }
        }
        return element
    }

    private fun createGlutterElement(): HTMLDivElement {
        val glutterElement = document.createElement("div") as HTMLDivElement
        glutterElement.className = "diff-glutters"
        return glutterElement
    }

    private fun createColorHelp(name: String) {
        val insertColor = document.createElement("div")
        insertColor.className = "color-help $name-color"
        colorsHelp.appendChild(insertColor)

        val insertText = document.createElement("span")
        insertText.className = "text"
        insertText.innerHTML = if (name.endsWith("e")) name + "d" else name + "ed"
        colorsHelp.appendChild(insertText)
    }
}