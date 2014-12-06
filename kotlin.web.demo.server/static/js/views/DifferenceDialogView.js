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
                var baseTextLines = difference.expected.split("</br>");
                var newTextLines = difference.actual.split("</br>");
                var sequenceMatcher = new difflib.SequenceMatcher(baseTextLines, newTextLines);
                dialogElement.innerHTML = "";
                dialogElement.appendChild(createDialogContent(baseTextLines, newTextLines, sequenceMatcher.get_opcodes()));
                //dialogElement.appendChild(diffview.buildView({
                //    baseTextLines: baseTextLines,
                //    newTextLines: newTextLines,
                //    opcodes: sequenceMatcher.get_opcodes(),
                //    baseTextName: "Expected",
                //    newTextName: "Actual",
                //    viewType: 0
                //}));
                $(dialogElement).dialog("open");
            }
        };

        var leftLineElements = [];
        var rightLineElements = [];

        function createDialogContent(expectedLines, actualLines, opCodes){
            var differenceElement = document.createElement("div");
            differenceElement.className = "difference-dialog-content";
            differenceElement.appendChild(createDifferenceElement(expectedLines, opCodes, false));
            differenceElement.appendChild(createDifferenceElement(actualLines, opCodes, true));
            return differenceElement;
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
                        $(lineElements[j]).addClass(change);
                    }
                } else{
                    for (var j = n; j < ne; ++j) {
                        $(lineElements[j]).addClass(change);
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

        var dialogElement = document.createElement("div");
        dialogElement.className = "difference-dialog";
        document.body.appendChild(dialogElement);
        dialogElement.title = "Comparison failure";


        $(dialogElement).dialog({
            minWidth:700,
            width: 700,
            height: 700,
            minHeight:700,
            autoOpen: false,
            modal: true
        });
        return instance;
    }

    return DifferenceDialogView;
})();