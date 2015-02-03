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
 * Created by Semyon.Atamas on 8/11/2014.
 */


var HelpDialogView = (function () {
    var dialogElement = document.createElement("div");
    dialogElement.title = "Help";

    var exampleHelpElement = document.createElement("div");
    exampleHelpElement.id = "example-help";
    dialogElement.appendChild(exampleHelpElement);

    var shortcutsHelpElement = document.createElement("div");
    shortcutsHelpElement.id = "shortcuts-help";
    dialogElement.appendChild(shortcutsHelpElement);

    $(dialogElement).keydown(function (event) {
        if (event.keyCode == 27 || event.keyCode == 13) { /*escape enter*/
            $(this).dialog("close");
        }
        event.stopPropagation();
    });

    $(dialogElement).dialog(
        {
            resizable: false,
            minWidth: 500,
            autoOpen: false,
            modal: true
        }
    );

    function HelpDialogView() {
        var instance = {
            open: function () {
                $(dialogElement).dialog("open");
            },
            updateProjectHelp: function (data) {
                if (data != null && data != "") {
                    exampleHelpElement.innerHTML = data;
                    exampleHelpElement.style.padding = "";
                    $(exampleHelpElement).find("a").attr("target", "_blank")
                } else {
                    exampleHelpElement.innerHTML = "";
                    exampleHelpElement.style.padding = "0";
                }
            },
            addShortcut: function (keyNames, description) {
                var shortcutElement = document.createElement("tr");
                shortcutsHelpElement.appendChild(shortcutElement);

                var shortcutKeyCombinationElement = document.createElement("td");
                shortcutKeyCombinationElement.className = "shortcutKeyCombination";
                shortcutElement.appendChild(shortcutKeyCombinationElement);

                var separator = "";
                for (var i = keyNames.length - 1; i >= 0; --i) {
                    if (separator != "") {
                        var separatorElement = document.createElement("div");
                        separatorElement.className = "shortcutSeparator";
                        separatorElement.innerHTML = separator;
                        shortcutKeyCombinationElement.appendChild(separatorElement);
                    }

                    var nameElement = document.createElement("div");
                    nameElement.className = "shortcutKeyName";
                    nameElement.innerHTML = keyNames[i];
                    shortcutKeyCombinationElement.appendChild(nameElement);

                    separator = "+";
                }

                var descriptionElement = document.createElement("td");
                shortcutElement.appendChild(descriptionElement);

                var descriptionTextElement = document.createElement("div");
                descriptionTextElement.className = "shortcutDescription";
                descriptionTextElement.innerHTML = description;
                descriptionElement.appendChild(descriptionTextElement);
            }
        };
        return instance;
    }

    return HelpDialogView;
})();