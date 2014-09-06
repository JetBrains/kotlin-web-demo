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
 * Created by Semyon.Atamas on 8/27/2014.
 */


var InputDialogView = (function () {

    function InputDialogView(title, inputText, buttonText) {
        var dialog = document.createElement("div");
        dialog.id = "input-dialog";

        var text = document.createElement("span");
        var input = document.createElement("input");

        dialog.appendChild(text);
        dialog.appendChild(input);

        dialog.title = title;
        text.innerHTML = inputText;

        $(dialog).dialog({
            modal: "true",
            width: 380,
            autoOpen: false
        });

        var instance = {
            open: function (callback) {
                $(dialog).dialog("option", "buttons",  [
                    {
                        text: buttonText,
                        click: function () {
                            callback(input.value);
                            $(this).dialog("close");
                        }
                    },
                    {
                        text: "Cancel",
                        click: function () {
                            $(this).dialog("close");
                        }
                    }
                ]
                );
                $(dialog).dialog("open");
            }
        };

        return instance;
    }

    return InputDialogView;
})();