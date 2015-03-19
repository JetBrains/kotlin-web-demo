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

/**
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/30/12
 * Time: 3:37 PM
 */

var RunProvider = (function () {

    function RunProvider(onSuccess, onFail) {

        var instance = {
            run: function (configuration, project) {
                run(configuration, project);
            }
        };

        function run(configuration, project) {
            if (configuration.type.runner == ConfigurationType.runner.JAVA) {
                runJava(project);
            } else {
                runJs(project);
            }
        }


        function runJava(project) {
            $.ajax({
                url: generateAjaxUrl("run"),
                context: document.body,
                success: function (data) {
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                onSuccess(data, project);
                            } else {
                                onFail(data);
                            }
                        } else {
                            onFail("Incorrect data format.")
                        }
                    } catch (e) {
                        console.log(e);
                    }
                },
                dataType: "json",
                type: "POST",
                data: {project: JSON.stringify(project)},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        if(jqXHR.responseText != null && jqXHR.responseText != ""){
                            onFail(jqXHR.responseText);
                        } else {
                            onFail(textStatus + " : " + errorThrown);
                        }
                    } catch (e) {
                        console.log(e)
                    }
                }
            });

        }

        function runJs(project) {
            Kotlin.modules = {stdlib: Kotlin.modules.stdlib, builtins: Kotlin.modules.builtins};
            loadJsFromServer(project);
        }

        function loadJsFromServer(project) {
            $.ajax({
                url: generateAjaxUrl("run"),
                context: document.body,
                success: function (data) {
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                var output = data.slice();
                                $(data).each(function (ind, element) {
                                    if (element.type == "generatedJSCode") {
                                        try {
                                            var dataJs = eval(element.text);
                                            output.push({"text": safe_tags_replace(dataJs), "type": "jsOut"});
                                        } catch (e) {
                                            output.push({"type": "jsException", exception: e});
                                        }
                                    }
                                });
                                onSuccess(output, project);
                            } else {
                                onFail(data);
                            }
                        } else {
                            onFail("Incorrect data format.");
                        }
                    } catch (e) {
                        console.log(e)
                    }
                },
                dataType: "json",
                type: "POST",
                data: {project: JSON.stringify(project)},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        if(jqXHR.responseText != null && jqXHR.responseText != ""){
                            onFail(jqXHR.responseText);
                        } else {
                            onFail(textStatus + " : " + errorThrown);
                        }
                    } catch (e) {
                        console.log(e)
                    }
                }
            });
        }

        return instance;
    }

    return RunProvider;
})();