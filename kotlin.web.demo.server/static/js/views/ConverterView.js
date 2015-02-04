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

    function ConverterView(converterModel) {
        var instance = {
            open: function(){
                $(popup).dialog("open");
                var height = $(popup).height();
                $(popup).find(".CodeMirror").height(height);
                $(popup).find(".CodeMirror-scroll").height(height);
                $(popup).find(".CodeMirror-gutter").height(height);
                $(popup).find(".CodeMirror-gutters").height(height);
                //javaEditor.refresh();
                //kotlinEditor.refresh();
            }
        };

        var popup = document.createElement("div");
        popup.id = "myPopupForConverterFromJavaToKotlin";
        popup.title = "Convert your java code to kotlin";
        document.body.appendChild(popup);

        var leftHalf = document.createElement("div");
        var javaCodeTextarea = document.createElement("textarea");
        leftHalf.appendChild(javaCodeTextarea);
        popup.appendChild(leftHalf);
        var javaEditor = CodeMirror.fromTextArea(javaCodeTextarea, {
            lineNumbers: true,
            styleActiveLine: true,
            matchBrackets: true,
            autoCloseBrackets: true,
            continueComments: true,
            extraKeys: {
                "Shift-Tab": false,
                "Ctrl-Alt-L": "indentAuto",
                "Ctrl-/": "toggleComment"
            },
            mode: "text/x-java",
            minHeight: "430px",
            tabSize: 4
        });

        var rightHalf = document.createElement("div");
        var kotlinCodeTextarea = document.createElement("textarea");
        rightHalf.appendChild(kotlinCodeTextarea);
        popup.appendChild(rightHalf);
        var kotlinEditor = CodeMirror.fromTextArea(kotlinCodeTextarea, {
            lineNumbers: true,
            matchBrackets: true,
            styleActiveLine: true,
            readOnly: true,
            mode: "text/kotlin",
            minHeight: "430px",
            tabSize: 4
        });

        $(popup).keydown(function (event) {
            if (event.keyCode == 27) { /*escape enter*/
                $(this).dialog("close");
            } else if (event.keyCode == 13 && (event.ctrlKey || event.metaKey)) { /*enter*/
                $(this).parent().find("button:eq(1)").trigger("click");
            }
            event.stopPropagation();
        });

        $(popup).dialog({
            modal: true,
            autoOpen: false,
            resizable: false,
            width: 700,
            height: 700,
            resizeStop: function (event, ui) {
                var height = popup.height();
                $(popup).find(".CodeMirror").height(height);
                $(popup).find(".CodeMirror-scroll").height(height);
                $(popup).find(".CodeMirror-gutter").height(height);
                $(popup).find(".CodeMirror-gutters").height(height);
                //javaEditor.refresh();
                //kotlinEditor.refresh();
            },
            buttons: [
                { text: "Convert to Kotlin",
                    click: function () {
                        converterModel.convert(javaEditor.getValue(), function(text){
                            kotlinEditor.setValue(text);
                            var last = kotlinEditor.lineCount();
                            kotlinEditor.operation(function() {
                                for (var i = 0; i < last; ++i) kotlinEditor.indentLine(i);
                            });
                        });
                    }
                }
            ]
        });


        return instance;
    }

    return ConverterView;
})();