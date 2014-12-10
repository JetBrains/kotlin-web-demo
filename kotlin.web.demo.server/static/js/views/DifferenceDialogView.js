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
 * Created by Semyon.Atamas on 12/4/2014.
 */

var DifferenceDialogView = (function () {
    function DifferenceDialogView() {
        var instance = {
            open:function(difference){
                leftLineElements = [];
                rightLineElements = [];
                var baseTextLines = difference.expected.split("</br>");
                var newTextLines = difference.actual.split("</br>");
                var sequenceMatcher = new difflib.SequenceMatcher(baseTextLines, newTextLines);
                createDialogContent(baseTextLines, newTextLines, sequenceMatcher.get_opcodes());
                $(dialogElement).dialog("open");
                $(differenceElement).height($(dialogElement).height() - $(colorsHelp).outerHeight(true));
            }
        };

        var leftLineElements = [];
        var rightLineElements = [];
        var dialogElement = document.createElement("div");
        dialogElement.className = "difference-dialog";
        document.body.appendChild(dialogElement);
        dialogElement.title = "Comparison failure";

        var differenceElement = document.createElement("div");
        dialogElement.appendChild(differenceElement);

        var colorsHelp = document.createElement("div");
        colorsHelp.className = "colors-help";
        dialogElement.appendChild(colorsHelp);
        createColorHelp("delete");
        createColorHelp("replace");
        createColorHelp("insert");

        $(dialogElement).keydown(function (event) {
            if (event.keyCode == 27) { /*escape enter*/
                $(this).dialog("close");
            }
            event.stopPropagation();
        });

        $(dialogElement).dialog({
            minWidth: 700,
            width: 700,
            height: 500,
            minHeight: 700,
            autoOpen: false,
            modal: true
        });

        function createDialogContent(expectedLines, actualLines, opCodes){
            differenceElement.innerHTML = "";
            differenceElement.className = "difference-dialog-content";
            differenceElement.appendChild(createDifferenceElement(expectedLines, opCodes, false));
            differenceElement.appendChild(createDifferenceElement(actualLines, opCodes, true));
        }

        function createDifferenceElement(lines, opCodes, isRightElement){
            var element = document.createElement("div");
            element.className = "diff";
            var glutter = createGlutterElement();
            element.appendChild(glutter);

            var outputElement = document.createElement("div");
            outputElement.className = "diff-output";
            element.appendChild(outputElement);

            var lineElements = isRightElement ? rightLineElements : leftLineElements;
            for(var i = 0; i < lines.length; ++i){
                var lineNumber = document.createElement("div");
                lineNumber.className = "diff-lineNumber";
                lineNumber.innerHTML = i + "";
                glutter.appendChild(lineNumber);

                var line = document.createElement("div");
                line.className = "diff-line";
                line.innerHTML = lines[i];
                outputElement.appendChild(line);

                lineElements.push(line);
            }

            for(var i = 0; i < opCodes.length; ++i){
                var code = opCodes[i];
                var change = code[0];
                var b = code[1];
                var be = code[2];
                var n = code[3];
                var ne = code[4];

                if(!isRightElement) {
                    for (var j = b; j < be; ++j) {
                        $(lineElements[j]).addClass(change + "-color");
                    }
                } else{
                    for (var j = n; j < ne; ++j) {
                        $(lineElements[j]).addClass(change + "-color");
                    }
                }
            }
            return element;
        }

        function createGlutterElement(){
            var glutterElement = document.createElement("div");
            glutterElement.className = "diff-glutters";
            return glutterElement;
        }

        function createColorHelp(name) {
            var insertColor = document.createElement("div");
            insertColor.className = "color-help " + name + "-color";
            colorsHelp.appendChild(insertColor);

            var insertText = document.createElement("span");
            insertText.className = "text";
            insertText.innerHTML = name.endsWith("e") ? name + "d" : name + "ed";
            colorsHelp.appendChild(insertText)
        }

        return instance;
    }

    return DifferenceDialogView;
})();