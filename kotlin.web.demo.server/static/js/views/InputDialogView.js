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

    var dialog, text, input;
    dialog = document.createElement("div");
    dialog.id = "input-dialog";

    text = document.createElement("span");
    text.id = "input-dialog-text";
    input = document.createElement("input");
    input.id = "input-dialog-input";


    dialog.appendChild(text);
    dialog.appendChild(input);


    $(dialog).dialog({
        modal: "true",
        width: 380,
        autoOpen: false
    });


    $(dialog).keydown(function (event) {
        if (event.keyCode == 13) { /*enter*/
            $(this).parent()
                .find("button:eq(1)").trigger("click");
        } else if(event.keyCode == 27){ /*escape*/
            $(this).dialog("close");
        }
        event.stopPropagation();
    });

    function InputDialogView(title, inputText, buttonText) {


        var instance = {

            open: function (callback, defaultValue) {

                input.oninput = function () {
                    if (instance.verify(input.value)){
                        enableInput()
                    } else{
                        disableInput()
                    }
                };

                if (defaultValue != null && defaultValue != undefined) {
                    var verifiedDefaultValue = defaultValue;
                    var i = 1;
                    while (!instance.verify(verifiedDefaultValue)) {
                        verifiedDefaultValue = defaultValue + i;
                        i = i + 1;
                    }
                    input.value = verifiedDefaultValue;
                }

                enableInput();

                $(dialog).dialog('option', 'title', title);
                text.innerHTML = inputText;
                $(dialog).dialog("option", "buttons", [
                        {
                            text: buttonText,
                            click: function (event) {
                                if (instance.verify(input.value)) {
                                    callback(input.value);
                                    $(this).dialog("close");
                                } else {
                                    disableInput();
                                }
                                event.stopPropagation();
                            }
                        },
                        {
                            text: "Cancel",
                            click: function (event) {
                                $(this).dialog("close");
                                event.stopPropagation();
                            }
                        }
                    ]
                );
                $(dialog).dialog("open");
            },
            verify: function (input) {
                return true;
            }
        };



        function disableInput(){
            input.style.outlineColor = "red";
            $(dialog).parent().find("button:eq(1)").button("option", "disabled", true);
            input.focus();
        }

        function enableInput(){
            input.style.outlineColor = "";
            $(dialog).parent().find("button:eq(1)").button("option", "disabled", false);
            input.focus();
        }

        return instance;
    }

    return InputDialogView;
})();