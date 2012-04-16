/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/30/12
 * Time: 3:37 PM
 */


var ConverterView = (function () {

    function ConverterView(element, converterModel) {
        var my_editor;
        var model = converterModel;

        $("body div:first").after("<div class=\"myPopupForConverterFromJavaToKotlin\" title=\"Enter Java code\"><textarea class=\"CodeMirror\" name=\"myTextareaForConverterFromJavaToKotlin\"></textarea></div>");

        var popup = $(".myPopupForConverterFromJavaToKotlin");

        var instance = {
            closeDialog:function () {
                popup.dialog("close");
            }
        };

        my_editor = CodeMirror.fromTextArea(document.getElementsByName("myTextareaForConverterFromJavaToKotlin")[0], {
            lineNumbers:true,
            matchBrackets:true,
            mode:"text/x-java",
            minHeight:"430px"
        });

        element.click(function () {
            var height = popup.dialog("option", "height") - 120;
            $("div #scroll", popup).css("height", height + "px");
            popup.dialog("open");
            my_editor.refresh();
        });

        popup.dialog({
            modal:"true",
            width:640,
            height:480,
            autoOpen:false,
            resizeStop:function (event, ui) {
                var height = popup.dialog("option", "height") - 120;
                $("div #scroll", popup).css("height", height + "px");
                my_editor.refresh();
            },
            buttons:[
                { text:"Convert to Kotlin",
                    click:function () {
                        model.convert(my_editor.getValue());
                    }
                },
                { text:"Cancel",
                    click:function () {
                        popup.dialog("close");
                    }
                }
            ]
        });

        return instance;
    }

    return ConverterView;
})();