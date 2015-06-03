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

import html4k.dom.append
import html4k.js.div
import jquery.jq
import jquery.ui.dialog
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import utils.KeyCode
import utils.jquery.height
import utils.jquery.outerHeight
import utils.jquery.ui.Dialog
import utils.jquery.keydown
import kotlin.browser.document
import kotlin.dom.addClass


native
object  difflib {
    class SequenceMatcher(baseTextLines: Array<String>, newTextLines: Array<String>){
        fun get_opcodes(): Array<dynamic>
    }
}

object DifferenceDialog{
    fun open(baseText: String, newText: String) {
        leftLineElements.clear();
        rightLineElements.clear();
        var baseTextLines = baseText.splitBy("</br>");
        var newTextLines = newText.splitBy("</br>");
        var sequenceMatcher = difflib.SequenceMatcher(baseTextLines.toTypedArray(), newTextLines.toTypedArray());
        createDialogContent(baseTextLines, newTextLines, sequenceMatcher.get_opcodes());
        jq(dialogElement).dialog("open");
        jq(differenceElement).height(jq(dialogElement).height().toInt() - jq(colorsHelp).outerHeight(true).toInt());
    }

    val leftLineElements = arrayListOf<HTMLElement>()
    val rightLineElements = arrayListOf<HTMLElement>()

    val dialogElement = document.body!!.append.div {
        classes = setOf("difference-dialog")
        title = "Comparison failure"
    }

    val differenceElement  = dialogElement.append.div {}

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
        createColorHelp("delete");
        createColorHelp("replace");
        createColorHelp("insert");

        jq(dialogElement).keydown({ event ->
            if (event.keyCode == KeyCode.ENTER.code) {
                dialog.close()
            }
            event.stopPropagation();
        });
    }

    fun createDialogContent(expectedLines: List<String>, actualLines: List<String>, opCodes: Array<dynamic>){
        differenceElement.innerHTML = "";
        differenceElement.className = "difference-dialog-content";
        differenceElement.appendChild(createDifferenceElement(expectedLines, opCodes, false));
        differenceElement.appendChild(createDifferenceElement(actualLines, opCodes, true));
    }

    fun createDifferenceElement(lines: List<String>, opCodes: Array<dynamic>, isRightElement: Boolean): HTMLDivElement{
        var element = document.createElement("div") as HTMLDivElement;
        element.className = "diff";
        var glutter = createGlutterElement();
        element.appendChild(glutter);

        var outputElement = document.createElement("div");
        outputElement.className = "diff-output";
        element.appendChild(outputElement);

        var lineElements = if(isRightElement) rightLineElements else leftLineElements;
        for(i in 0..lines.size() - 1){
            var lineNumber = document.createElement("div");
            lineNumber.className = "diff-lineNumber";
            lineNumber.innerHTML = i.toString() + "";
            glutter.appendChild(lineNumber);

            var line = document.createElement("div") as HTMLDivElement;
            line.className = "diff-line";
            line.innerHTML = lines[i];
            outputElement.appendChild(line);

            lineElements.add(line);
        }

        for(i in 0..opCodes.size() - 1){
            var code = opCodes[i];
            var change = code[0];
            var b = (code[1] as Number).toInt();
            var be = (code[2] as Number).toInt();
            var n = (code[3] as Number).toInt();
            var ne = (code[4] as Number).toInt();

            if(!isRightElement) {
                for (j in b..be - 1) {
                    lineElements[j].addClass(change + "-color");
                }
            } else{
                for (j in n..ne - 1) {
                    lineElements[j].addClass(change + "-color");
                }
            }
        }
        return element;
    }

    fun createGlutterElement(): HTMLDivElement{
        var glutterElement = document.createElement("div") as HTMLDivElement;
        glutterElement.className = "diff-glutters";
        return glutterElement;
    }

    fun createColorHelp(name: String) {
        var insertColor = document.createElement("div");
        insertColor.className = "color-help " + name + "-color";
        colorsHelp.appendChild(insertColor);

        var insertText = document.createElement("span");
        insertText.className = "text";
        insertText.innerHTML = if (name.endsWith("e")) name + "d" else name + "ed";
        colorsHelp.appendChild(insertText)
    }
}