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

var RunProvider = (function () {

    function RunProvider(onSuccess, onFail) {

        var instance = {
            run: function (configuration, programText, args, example) {
                run(configuration, programText, args, example);
            }
        };

        function run(configuration, project, args) {
            if (configuration.type.runner == ConfigurationType.runner.JAVA) {
                runJava(configuration, project, args);
            } else {
                runJs(configuration, project, args);
            }
        }


        function runJava(configuration, project, args) {
            var confTypeString = Configuration.getStringFromType(configuration.type);
            $.ajax({
                url: generateAjaxUrl("run", confTypeString),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            onSuccess(data);
                        } else {
                            onFail(data);
                        }
                    } else {
                        onFail("Incorrect data format.")
                    }
                },
                dataType: "json",
                type: "POST",
                data: {project: JSON.stringify(project)},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    onFail(textStatus + " : " + errorThrown);
                }
            });

        }

        function runJs(configuration, programText, args) {
            Kotlin.modules = {};
            loadJsFromServer(configuration, programText, args);
        }

        function loadJsFromServer(configuration, i, arguments) {
            var confTypeString = Configuration.getStringFromType(configuration.type);
            $.ajax({
                url: generateAjaxUrl("run", confTypeString),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            var dataJs;
                            try {
                                dataJs = eval(data[0].text);
                            } catch (e) {
                                onFail(e);
                                return;
                            }
                            var output = [
                                {"text": safe_tags_replace(dataJs), "type": "out"},
                                {"text": data[0].text, "type": "toggle-info"}
                            ];
                            onSuccess(output);
                        } else {
                            onFail(data);
                        }
                    } else {
                        onFail("Incorrect data format.");
                    }
                },
                dataType: "json",
                type: "POST",
                data: {text: i, consoleArgs: arguments},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    onFail(textStatus + " : " + errorThrown);
                }
            });
        }

        return instance;
    }

    return RunProvider;
})();