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

    var dialog = document.createElement("div");
    dialog.id = "input-dialog";

    var text = document.createElement("span");
    text.id = "input-dialog-text";
    dialog.appendChild(text);

    var input = document.createElement("input");
    input.id = "input-dialog-input";
    input.type = "text";
    dialog.appendChild(input);

    var message = document.createElement("div");
    dialog.appendChild(message);
    message.className = "input-dialog-error-message";

    $(dialog).dialog({
        resizable: false,
        modal: "true",
        width: 380,
        autoOpen: false,
        open: function(){
            input.select();
        }
    });


    $(dialog).keydown(function (event) {
        if (event.keyCode == 13) { /*enter*/
            $(this).parent()
                .find("button:eq(1)").trigger("click");
        } else if (event.keyCode == 27) { /*escape*/
            $(this).dialog("close");
        }
        event.stopPropagation();
    });

    function InputDialogView(title, inputText, buttonText) {
        var instance = {

            open: function (callback, defaultValue) {

                input.focus();

                validationResult({valid: true});

                input.oninput = function () {
                    validationResult(instance.validate(input.value));
                };

                if (defaultValue != null && defaultValue != undefined) {
                    var verifiedDefaultValue = defaultValue;
                    var i = 1;
                    while (!instance.validate(verifiedDefaultValue).valid) {
                        verifiedDefaultValue = defaultValue + i;
                        ++i;
                    }
                    input.value = verifiedDefaultValue;
                    //input.select();
                }

                $(dialog).dialog('option', 'title', title);
                text.innerHTML = inputText;
                $(dialog).dialog("option", "buttons", [
                        {
                            text: buttonText,
                            click: function (event) {
                                var validationResult = instance.validate(input.value);
                                if (validationResult.valid) {
                                    callback(input.value);
                                    $(this).dialog("close");
                                } else {
                                    validationResult(validationResult);
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
            validate: function (input) {
                return {valid: true};
            }
        };

        function validationResult(result) {
            input.style.outlineColor = result.valid ? "" : "red";
            $(dialog).parent().find("button:eq(1)").button("option", "disabled", !result.valid);
            if (!result.valid && result.message != null) {
                message.innerHTML = result.message;
            } else {
                message.innerHTML = "";
            }
            input.focus();
        }

        return instance;
    }

    return InputDialogView;
})();