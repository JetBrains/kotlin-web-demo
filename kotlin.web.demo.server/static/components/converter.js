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
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 2/2/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */

var Converter = (function () {
    function Converter() {
        var instance = {

        };

        return instance;
    }


    Converter.convert = function () {
        var i = editorForJava.getValue();
        setConsoleMessage("");
        setStatusBarMessage("Converting...");
        $.ajax({
            url:generateAjaxUrl("convertToKotlin", ""),
            context:document.body,
            success:onConvertSuccess,
            dataType:"json",
            type:"POST",
            data:{text:i},
            timeout:10000,
            error:function () {
                $("#convertToKotlinDialog").dialog("close");
                setStatusBarError(CONVERT_REQUEST_ABORTED);
                setConsoleMessage(CONVERT_REQUEST_ABORTED);
            }
        });
    };

    function onConvertSuccess(data) {
        $("#convertToKotlinDialog").dialog("close");
        editor.focus();
        editor.setOption("mode", "kotlin");
        if (data != null && typeof data != "undefined") {
            if (data[0] != null && typeof data[0] != "undefined") {
                if (typeof data[0].exception != "undefined") {
                    setStatusBarError(data[0].exception);
                    $("#tabs").tabs("select", 1);
                    setConsoleMessage(data[0].exception);
                } else {
                    editor.setValue(data[0].text);
                    editor.setSelection({line:0, ch:0}, {line:editor.lineCount() - 1});
                    editor.indentSelection("smart");
                    setStatusBarMessage(LOADING_CONVERSATION_TO_KOTLIN_OK);
                }
            }
        }
        setEditorState(false);
    }


    return Converter;
})();