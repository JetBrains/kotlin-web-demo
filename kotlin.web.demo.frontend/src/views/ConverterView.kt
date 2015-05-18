/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package views

import ConverterProvider
import CodeMirror
import jquery.jq
import jquery.ui.dialog
import kotlin.browser.document
import html4k.js.*
import html4k.dom.*
import kotlin.browser.window


/**
 * Created by Semyon.Atamas on 5/18/2015.
 */

class ConverterView(converterProvider: ConverterProvider) {

    fun open() {
        jq(popup).dialog("open");
        javaEditor.refresh();
        kotlinEditor.refresh();
    }

    var popup = document.body!!.append.div {
        id = "myPopupForConverterFromJavaToKotlin"
        title = "Convert your java code to kotlin"
    }

    var leftHalf = popup.append.div {};
    var javaCodeTextarea = leftHalf.append.textArea {};
    var javaEditor = CodeMirror.fromTextArea(javaCodeTextarea, json(
            "lineNumbers" to true,
            "styleActiveLine" to true,
            "matchBrackets" to true,
            "autoCloseBrackets" to true,
            "continueComments" to true,
            "extraKeys" to json(
                    "Shift-Tab" to false,
                    "Ctrl-Alt-L" to "indentAuto",
                    "Ctrl-/" to "toggleComment"
            ),
            "mode" to "text/x-java",
            "tabSize" to 4
    ));

    var rightHalf = popup.append.div {};
    var kotlinCodeTextarea = rightHalf.append.textArea {}
    var kotlinEditor = CodeMirror.fromTextArea(kotlinCodeTextarea, json(
            "lineNumbers" to true,
            "matchBrackets" to true,
            "styleActiveLine" to true,
            "readOnly" to true,
            "mode" to "text/kotlin",
            "tabSize" to 4
    ));

    init {
        jq(popup).keydown({ event ->
            if (event.keyCode == 27) {
                /*escape enter*/
                jq(popup).dialog("close");
            } else if (event.keyCode == 13 && (event.ctrlKey || event.metaKey)) {
                /*enter*/
                jq(popup).parent().find("button:eq(1):enabled").click();
            }
            event.stopPropagation();
        }) ;
        jq(popup).dialog(json(
                "modal" to true,
                "autoOpen" to false,
                "resizable" to true,
                "width" to 700,
                "height" to 700,
                "resizeStop" to {
                    javaEditor.refresh();
                    kotlinEditor.refresh();
                },
                "buttons" to arrayOf(
                        json(
                                "text" to "Convert to Kotlin",
                                "click" to {
                                    converterProvider.convert(javaEditor.getValue(), { text ->
                                        kotlinEditor.setValue(text);
                                        var lineCount: Int = kotlinEditor.lineCount();
                                        kotlinEditor.operation({
                                            for (i in 0..lineCount) kotlinEditor.indentLine(i);
                                        });
                                    });
                                }
                        )
                )
        )) ;
        converterProvider.beforeConvert = {
            jq(popup).next().find(".ui-dialog-buttonset button").button("disable");
        };

        converterProvider.onConvertFail = { errors ->
            for (error in errors) {
                window.alert(error.exception);
            }
        };

        converterProvider.onConvertComplete = {
            jq(popup).next().find(".ui-dialog-buttonset button").button("enable");
        };
    }
}