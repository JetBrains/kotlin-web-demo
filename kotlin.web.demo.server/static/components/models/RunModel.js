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

/* EVENTS:
 get_highlighting
 run_java
 run_js
 */

var RunModel = (function () {
    var eventHandler = new EventsHandler();

    var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.dependencies.JAVA, Configuration.runner.JAVA);

    function RunModel() {

        var instance = {
            addListener:function (name, f) {
                eventHandler.addListener(name, f);
            },
            fire:function (name, param) {
                eventHandler.fire(name, param);
            },
            run:function () {
                checkHighlightingBeforeRun();
            },
            processHighlighting:function (status, highlightingResult) {
                if (status) {
                    run(highlightingResult);
                } else {
                    eventHandler.fire("run_java", false, null);
                    eventHandler.fire("run_js", false, null);
                }
            },
            changeConfiguration:function (status, conf) {
                if (status) configuration = conf;
            }
        };

        return instance;
    }

    RunModel.getArguments = null;
    RunModel.getProgramText = null;

    function checkHighlightingBeforeRun() {
        isRunInProgress = true;
        var parameters = [];
        parameters.push(configuration.dependencies, Configuration.mode.SERVER, RunModel.getProgramText());
        eventHandler.fire("get_highlighting", parameters);
    }

    function run(highlightingResult) {
        if (isRunInProgress) {
            isRunInProgress = false;
            if (!checkIfThereAreErrorsInData(highlightingResult)) {
                if (configuration.runner == Configuration.runner.JAVA) {
                    runJava();
                } else {
                    runJs();
                }
            } else {
                eventHandler.fire("run_java", false, null);
                eventHandler.fire("run_js", false, null);
            }
        }
    }

    var isRunInProgress = false;

    var isCompiling = false;

    function runJava() {
        if (!isCompiling) {
            isCompiling = true;
            var i = RunModel.getProgramText();
            var arguments = RunModel.getArguments();
            $.ajax({
                url:RequestGenerator.generateAjaxUrl("run", configuration.runner),
                context:document.body,
                success:function (data) {
                    isCompiling = false;
                    eventHandler.fire("run_java", true, data);
                },
                dataType:"json",
                type:"POST",
                data:{text:i, consoleArgs:arguments},
                timeout:10000,
                error:function () {
                    isCompiling = false;
                    eventHandler.fire("run_java", false, null);
                }
            });

        }
    }

    var COMPILE_IN_JS_APPLET_ERROR = "The Pre-Alpha JavaScript back-end could not generate code for this program.<br/>Try to run it using JVM.";

    function runJs() {
        var arguments = RunModel.getArguments();
        var programText = RunModel.getProgramText();
        if (!isCompiling) {
            isCompiling = true;
            if (configuration.mode == Configuration.mode.CLIENT) {
                try {
                    var dataFromApplet;
                    try {
                        dataFromApplet = $("#myapplet")[0].translateToJS(programText, arguments);
                    } catch (e) {
                        loadJsFromServer(programText, arguments);
                        return;
                    }
                    isCompiling = false;
                    eventHandler.fire("run_js", true, dataFromApplet);
                } catch (e) {
                    isCompiling = false;
                    eventHandler.fire("run_js", false, e);
                }
            } else {
                loadJsFromServer(programText, arguments);
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


    function loadJsFromServer(i, arguments) {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("run", configuration.dependencies),
            context:document.body,
            success:function (data) {
                isCompiling = false;
                eventHandler.fire("run_js", true, data);
            },
            dataType:"json",
            type:"POST",
            data:{text:i, consoleArgs:arguments},
            timeout:10000,
            error:function () {
                isCompiling = false;
                eventHandler.fire("run_js", false, null);
            }
        });
    }

    return RunModel;
})();