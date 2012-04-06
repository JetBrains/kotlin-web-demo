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

var ConsoleView = (function () {

    var generatedJsCode = "";
    var COMPILE_IN_JS_APPLET_ERROR = "The Pre-Alpha JavaScript back-end could not generate code for this program.<br/>Try to run it using JVM.";
    var SHOW_JAVASCRIPT_CODE = "Show generated JavaScript code";

    var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.dependencies.JAVA, Configuration.runner.JAVA);

    function ConsoleView() {

        var instance = {
            processOutput:function (status, data) {
                if (status) updateConsole(data);
            },
            processOutputForJs:function (status, data) {
                if (status) updateConsoleForJs(data[0].text);
            },
            changeConfiguration:function (status, conf) {
                if (status) configuration = conf;
            },
            writeException:function (data) {
                writeException(data);
            }
        };


        $("#tabs").tabs();

        $("#popupForCanvas").dialog({
            width:630,
            height:350,
            autoOpen:false,
            close:function () {
                $("#popupForCanvas").html("");
            }
        });

        return instance;
    }

    function writeException(data) {
        if (typeof data != "undefined" && typeof data[0] != "undefined" && typeof data[0].exception != "undefined") {
            $("#console").html("");
            $("#tabs").tabs("select", 1);
            var i = 0;
            while (typeof data[i] != "undefined") {
                createException(data[i]);
                i++;
            }
        } else if (typeof data != "undefined" || data == null) {
        } else {
            $("#console").html("");
            $("#tabs").tabs("select", 1);
            $("#console").html("ERROR: " + data);
        }
    }

    function createException(ex) {
        var console = document.createElement("div");
        if (ex.type == "out") {
            document.getElementById("console").appendChild(createElementForConsole("STACKTRACE", null, unEscapeString(ex.exception)));
        } else {
            document.getElementById("console").appendChild(createElementForConsole("ERROR", null, unEscapeString(ex.exception)));
        }
    }

    function createElementForConsole(severity, start, title) {
        var p = document.createElement("p");
        if (severity == 'WARNING') {
            if (title.indexOf("is never used") > 0) {
                p.className = "problemsViewWarningNeverUsed";
            } else {
                p.className = "problemsViewWarning";
            }

        } else if (severity == 'STACKTRACE') {
            p.className = "problemsViewStacktrace";
        } else {
            p.className = "problemsViewError";
        }
        var titleDiv = document.createElement("span");
        if (start == null) {
            titleDiv.innerHTML = " " + unEscapeString(title);
        } else {
            titleDiv.innerHTML = "(" + (start.line + 1) + ", " + (start.ch + 1) + ") : " + unEscapeString(title);
        }
        p.appendChild(titleDiv);
        return p;
    }


    ConsoleView.showJsCode = function () {
        document.getElementById("console").removeChild(document.getElementById("console").childNodes[1]);
        $("#console :first-child").after("<p class='consoleViewInfo'>" + generatedJsCode + "</p>");
    };


    function updateConsoleForJs(data) {
        $("#console").html("");
        $("#tabs").tabs("select", 1);

        var dataJs;
        if (data.indexOf("exception=") == 0) {
            writeException(data);
        } else {
            $("#popupForCanvas").html("");
            $("#popupForCanvas").append("<canvas width=\"" + $("#popupForCanvas").dialog("option", "width")
                + "\" height=\"" + ($("#popupForCanvas").dialog("option", "height") - 50) + "\" id=\"mycanvas\"></canvas>");
            if (configuration.dependencies == "canvas") {
                $("#popupForCanvas").dialog("open");
            }
            try {
                dataJs = eval(data);
            } catch (e) {
                writeException(e);
                return;
            }
            generatedJsCode = data;
            $("#console").html("<p>" + safe_tags_replace(dataJs)
                + "</p><p class='consoleViewInfo'><a href='javascript:void(0);' onclick='ConsoleView.showJsCode();'>"
                + SHOW_JAVASCRIPT_CODE + "</a></p>");
        }
    }


    function setConsoleMessage(message) {
        $("#console").html(message);
    }

    function updateConsole(data) {
        $("#console").html("");
        $("#tabs").tabs("select", 1);

        if (data != null) {
            if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
                writeException(data);
            } else {
                $("#tabs").tabs("select", 1);
                var i = 0;
                var errors = document.createElement("div");
                while (typeof data[i] != "undefined") {
                    var p = document.createElement("p");
                    if ((data[i].type == "err") && (data[i].text != "")) {
                        p.className = "consoleViewError";
                    }
                    if (data[i].type == "info") {
                        p.className = "consoleViewInfo";
                    }
                    p.innerHTML = unEscapeString(data[i].text);
                    errors.appendChild(p);
                    i++;
                }
                document.getElementById("console").appendChild(errors);
            }
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

    return ConsoleView;
})();