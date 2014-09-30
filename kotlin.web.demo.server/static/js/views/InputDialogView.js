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
    $(input).on( "input", function(){
        this.style.outlineColor = "";
        $(dialog).parent().find("button:eq(1)").button( "option", "disabled", false );
    });

    dialog.appendChild(text);
    dialog.appendChild(input);




    $(dialog).dialog({
        modal: "true",
        width: 380,
        autoOpen: false
    });

    //now first button is default button
    $(dialog).keydown(function (event) {
        if (event.keyCode == 13) {
            $(this).parent()
                .find("button:eq(1)").trigger("click");
            return false;
        }
        event.stopPropagation();
    });

    function InputDialogView(title, inputText, buttonText) {


        var instance = {

            open: function (callback, verificator) {
                input.style.outlineColor = "";
                $(dialog).parent().find("button:eq(1)").button( "option", "disabled", true );
                $(dialog).dialog('option', 'title', title);
                text.innerHTML = inputText;
                $(dialog).dialog("option", "buttons", [
                        {
                            text: buttonText,
                            click: function (event) {
                                if (verificator != undefined && verificator != null) {
                                    if(verificator(input.value)){
                                        callback(input.value);
                                        $(this).dialog("close");
                                    } else{
                                        input.style.outlineColor = "red";
                                        $(this).parent().find("button:eq(1)").button( "option", "disabled", true );
                                        input.focus();
                                    }
                                } else{
                                    callback(input.value);
                                    $(this).dialog("close");
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
            }
        };

        return instance;
    }

    return InputDialogView;
})();