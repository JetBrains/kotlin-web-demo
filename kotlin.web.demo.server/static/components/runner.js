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

var Runner = (function () {
    function Runner() {
        var instance = {

        };

        return instance;
    }

//    var lastSelectedExample = 0;
    var isCompile = false;

    Runner.run = function () {
        setRunConfigurationMode();
        $("#run").css({opacity:0.5});
        if (runConfiguration.mode == "java") {
            runJava();
        } else {
            runJs();
        }

    };

    var counterSetConfMode = 0;

    function runJava() {
        var i = editor.getValue();
        setConsoleMessage("");
        setStatusBarMessage("Running...");
        if ($("#nohighlightingcheckbox").attr('checked') == 'checked') {
            Highlighting.sendHighlightingRequest(onHighlightingSuccessWait);
        } else {
            getErrors();
            onHighlightingSuccessWait(null);
        }
    }

    function runJs() {
        var i = editor.getValue();
        Highlighting.setLoadingErrors(true);
        if (isApplet) {
            var dataFromApplet;
            var data;
            try {
                dataFromApplet = $("#myapplet")[0].getHighlighting(i, runConfiguration.mode);
                Highlighting.setLoadingErrors(false);
            } catch (e) {
                Highlighting.setLoadingErrors(false);
                Highlighting.sendHighlightingRequest(onHighlightingSuccessWaitAfterConvertToJs);
                return;
            }
            data = eval(dataFromApplet);
            if (data != null || data[0] != null) {
                onHighlightingSuccessWaitAfterConvertToJs(data);
            }
        }
        else {
            Highlighting.sendHighlightingRequest(onHighlightingSuccessWaitAfterConvertToJs);
        }
    }

    function onHighlightingSuccessWait(data) {
        Highlighting.setLoadingErrors(false);
        Highlighting.onHighlightingSuccess(data);
        if (data == null || !checkIfThereAreErrorsInData(data)) {
            setStatusBarMessage("Running...");
            if (!isCompile) {
                isCompile = true;
                var i = editor.getValue();
                var arguments = $("#arguments").val();
                $.ajax({
                    url:generateAjaxUrl("run", runConfiguration.mode),
                    context:document.body,
                    success:onCompileSuccess,
                    dataType:"json",
                    type:"POST",
                    data:{text:i, consoleArgs:arguments},
                    timeout:10000,
                    error:function () {
                        $("#run").css({opacity:1});
                        isCompile = false;
                        setStatusBarError(RUN_REQUEST_ABORTED);
                        setConsoleMessage(RUN_REQUEST_ABORTED);
                    }
                });

            }
        }
    }

    function checkIfThereAreErrorsInData(data) {
        var i = 0;
        while (typeof data[i] != "undefined") {
            var severity = data[i].severity;
            if (severity == "ERROR") {
                return true;
            }
            i++;
        }
        return false;
    }

    function onHighlightingSuccessWaitAfterConvertToJs(data) {
        var arguments = $("#arguments").val();
        var i = editor.getValue();
        setConsoleMessage("");
        Highlighting.setLoadingErrors(false);
        Highlighting.onHighlightingSuccess(data);
        if (data == null || !checkIfThereAreErrorsInData(data)) {
            setStatusBarMessage("Running...");
            if (!isCompile) {
                $("#tabs").tabs("select", 1);
                isCompile = true;
                if (isApplet && isJsApplet) {
                    try {
                        var dataFromApplet;
                        try {
                            dataFromApplet = $("#myapplet")[0].translateToJS(i, arguments);
                        } catch (e) {
                            loadJsFromServer(i, arguments);
                            return;
                        }
                        isCompile = false;
                        var dataJs;
                        if (dataFromApplet.indexOf("exception=") == 0) {
                            dataJs = dataFromApplet.substring(10, dataFromApplet.length);
                            dataJs = createRedElement(COMPILE_IN_JS_APPLET_ERROR + "<br/>" + dataJs);
                            setStatusBarMessage(ERROR_UNTIL_EXECUTE);
                            setConsoleMessage("<p>" + dataJs + "</p>");
                        } else {
                            $("#popupForCanvas").html("");
                            $("#popupForCanvas").append("<canvas width=\"" + $("#popupForCanvas").dialog("option", "width")
                                + "\" height=\"" + ($("#popupForCanvas").dialog("option", "height") - 50) + "\" id=\"mycanvas\"></canvas>");
                            if (runConfiguration.mode == "canvas") {
                                $("#popupForCanvas").dialog("open");
                            }
                            dataJs = eval(dataFromApplet);
                            setStatusBarMessage(EXECUTE_OK);
                            generatedJSCode = dataFromApplet;
                            setConsoleMessage("<p>" + safe_tags_replace(dataJs) + "</p><p class='consoleViewInfo'><a href='javascript:void(0);' onclick='showJsCode();'>" + SHOW_JAVASCRIPT_CODE + "</a></p>");
                        }
                    } catch (e) {
                        isCompile = false;
                        setStatusBarMessage(ERROR_UNTIL_EXECUTE);
                        setConsoleMessage(createRedElement(COMPILE_IN_JS_APPLET_ERROR + "<br/>" + e));
                    }
                } else {
                    loadJsFromServer(i, arguments);
                }

            }
        } else {
            $("#tabs").tabs("select", 0);
            setStatusBarMessage(TRY_RUN_CODE_WITH_ERROR);
        }
    }

    function loadJsFromServer(i, arguments) {
        isJsApplet = false;
        $.ajax({
            url:generateAjaxUrl("run", runConfiguration.mode),
            context:document.body,
            success:onConvertToJsSuccess,
            dataType:"json",
            type:"POST",
            data:{text:i, consoleArgs:arguments},
            timeout:10000,
            error:function () {
                $("#run").css({opacity:1});
                isCompile = false;
                setStatusBarMessage(RUN_REQUEST_ABORTED);
                setConsoleMessage(RUN_REQUEST_ABORTED);
            }
        });
    }

    function onConvertToJsSuccess(data) {
        $("#run").css({opacity:1});
        isCompile = false;
        try {
            var genData;
            $("#tabs").tabs("select", 1);
            if (data != null) {
                if (typeof data[0].exception != "undefined") {
                    genData = createRedElement(COMPILE_IN_JS_APPLET_ERROR + "<br/>" + data[0].exception);
                    setStatusBarMessage(ERROR_UNTIL_EXECUTE);
                    setConsoleMessage("<p>" + genData + "</p>");
                } else if (typeof data[0].text != "undefined") {
                    $("#popupForCanvas").html("");
                    $("#popupForCanvas").append("<canvas width=\"" + $("#popupForCanvas").dialog("option", "width")
                        + "\" height=\"" + ($("#popupForCanvas").dialog("option", "height") - 50) + "\" id=\"mycanvas\"></canvas>");
                    if (runConfiguration.mode == "canvas") {
                        $("#popupForCanvas").dialog("open");
                        /*$("#popupForCanvas").dialog({
                            resize: function() {
                                $("#popupForCanvas").html("");
                                $("#popupForCanvas").append("<canvas width=\"" + $("#popupForCanvas").dialog("option", "width")
                                    + "\" height=\"" + ($("#popupForCanvas").dialog("option", "height") - 50) + "\" id=\"mycanvas\"></canvas>");
//                                $("#mycanvas")[0].width = $("#popupForCanvas").dialog("option", "width") - 100;
//                                $("#mycanvas")[0].height = $("#popupForCanvas").dialog("option", "height") - 100;
                                eval(data[0].text);
                            }
                        });*/

                    }
                    genData = eval(data[0].text);
                    setStatusBarMessage(EXECUTE_OK);
                    generatedJSCode = data[0].text;
                    setConsoleMessage("<p>" + safe_tags_replace(genData) +
                        "</p><p class='consoleViewInfo'><a href='javascript:void(0);' onclick='showJsCode();'>"
                        + SHOW_JAVASCRIPT_CODE + "</a></p>");
                }

            }
        } catch (e) {
            setStatusBarMessage(ERROR_UNTIL_EXECUTE);
        }
    }

    function onCompileSuccess(data) {
        $("#run").css({opacity:1});
        setStatusBarMessage("Loading output...");
        isCompile = false;
        var isCompiledWithErrors = false;
        if (data != null) {
            if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
                $("#tabs").tabs("select", 0);
                clearProblemView();
                setStatusBarMessage(data[0].exception);
                var j = 0;
                while (typeof data[j] != "undefined") {
                    createException(data[j]);
                    j++;
                }
                return;
            } else {
                $("#tabs").tabs("select", 1);
                var i = 0;
                var errors = document.createElement("div");
                while (typeof data[i] != "undefined") {
                    //If there is a compilation error
                    if (typeof data[i].message != "undefined") {
                        getErrors();
                        isCompiledWithErrors = true;
                        $("#tabs").tabs("select", 0);
                        //errors.appendChild(createElementForProblemView(data[i].type, null, data[i].message));
                    } else {
                        var p = document.createElement("p");
                        if ((data[i].type == "err") && (data[i].text != "")) {
                            p.className = "consoleViewError";
                            isCompiledWithErrors = true;
                        }
                        if (data[i].type == "info") {
                            p.className = "consoleViewInfo";
                        }
                        p.innerHTML = unEscapeString(data[i].text);
                        errors.appendChild(p);
                    }
                    i++;
                }
                document.getElementById("console").appendChild(errors);
            }
        }
        if (!isCompiledWithErrors) {
            setStatusBarMessage(EXECUTE_OK);
        } else {
            setStatusBarError(ERROR_UNTIL_EXECUTE);
        }
    }

    function createRedElement(text) {
        var div = document.createElement("div");
        var p = document.createElement("p");
        p.className = "consoleViewError";
        p.innerHTML = text;
        div.appendChild(p);
        return div.innerHTML;
    }


    return Runner;
})();