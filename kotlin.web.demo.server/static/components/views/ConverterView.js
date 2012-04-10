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
 * To change this template use File | Settings | File Templates.
 */


var ConverterView = (function () {

    var my_editor;
    var model = new ConverterModel();

    function ConverterView() {

        var instance = {
            closeDialog:function () {
                $("#convertToKotlinDialog").dialog("close");
            }
        };

        my_editor = CodeMirror.fromTextArea(document.getElementById("codeOnJava"), {
            lineNumbers:true,
            matchBrackets:true,
            mode:"text/x-java",
            minHeight:"430px"
        });

        $("#javaToKotlin").click(function () {
            var height = $("#convertToKotlinDialog").dialog("option", "height") - 120;
            $("div#convertToKotlinDialog div #scroll").css("height", height + "px");
            $("#convertToKotlinDialog").dialog("open");
            my_editor.refresh();
        });

        $("#whatimgjavatokotlin").click(function () {
            $("#dialogAboutJavaToKotlinConverter").dialog("open");
        });

        $("#dialogAboutJavaToKotlinConverter").dialog({
            modal:"true",
            width:300,
            autoOpen:false
        });

        $("#convertToKotlinDialog").dialog({
            modal:"true",
            width:640,
            height:480,
            autoOpen:false,
            resizeStop:function (event, ui) {
                var height = $("#convertToKotlinDialog").dialog("option", "height") - 120;
                $("div#convertToKotlinDialog div #scroll").css("height", height + "px");
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
                        $("#convertToKotlinDialog").dialog("close");
                    }
                }
            ]
        });

        return instance;
    }

    function processResult(data) {
    }

    return ConverterView;
})();