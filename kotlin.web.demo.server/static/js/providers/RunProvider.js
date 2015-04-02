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

    function RunProvider(onFail) {

        var instance = {
            onSuccess: function () {

            },
            onErrorsFound: function () {

            },
            onFail: function () {

            },
            onComplete: function(){

            },
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

        function checkDataForErrors(data){
            return data.some(function (element) {
                if(element.type == "errors"){
                    var containsErrors = false;
                    for(var fileName in element.errors){
                        if(element.errors[fileName].some(function(element){
                                return element.severity == "ERROR";
                            })){
                            containsErrors = true;
                        }
                    }
                    return !containsErrors
                } else {
                    return true;
                }
            })
        }


        function runJava(project) {
            $.ajax({
                //runConf is unused parameter. It's added to url for useful access logs
                url: generateAjaxUrl("run", {runConf: project.getConfiguration()}),
                context: document.body,
                success: function (data) {
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                if(checkDataForErrors(data)){
                                    instance.onSuccess(data, project);
                                } else {
                                    instance.onErrorsFound(data, project);
                                }
                            } else {
                                instance.onFail(data);
                            }
                        } else {
                            instance.onFail("Incorrect data format.")
                        }
                    } catch (e) {
                        console.log(e);
                    }
                },
                dataType: "json",
                type: "POST",
                data: {project: JSON.stringify(project)},
                timeout: 10000,
                complete: instance.onComplete,
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        if(jqXHR.responseText != null && jqXHR.responseText != ""){
                            instance.onFail(jqXHR.responseText);
                        } else {
                            instance.onFail(textStatus + " : " + errorThrown);
                        }
                    } catch (e) {
                        console.log(e)
                    }
                }
            });

        }

        function runJs(project) {
            loadJsFromServer(project);
        }

        function loadJsFromServer(project) {
            var originalKotlinModules = Kotlin.modules;
            $.ajax({
                //runConf is unused parameter. It's added to url for useful access logs
                url: generateAjaxUrl("run", {runConf: project.getConfiguration()}),
                context: document.body,
                success: function (data) {
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                if(checkDataForErrors(data)) {
                                    var output = data.slice();
                                    $(data).each(function (ind, element) {
                                        if (element.type == "generatedJSCode") {
                                            try {
                                                Kotlin.modules = {stdlib: Kotlin.modules.stdlib, builtins: Kotlin.modules.builtins};
                                                var dataJs = eval(element.text);
                                                Kotlin.modules = originalKotlinModules;
                                                output.push({"text": safe_tags_replace(dataJs), "type": "jsOut"});
                                            } catch (e) {
                                                output.push({"type": "jsException", exception: e});
                                            }
                                        }
                                    });
                                    instance.onSuccess(output, project);
                                } else{
                                    instance.onErrorsFound(data, project)
                                }
                            } else {
                                instance.onFail(data);
                            }
                        } else {
                            instance.onFail("Incorrect data format.");
                        }
                    } catch (e) {
                        console.log(e)
                    }
                },
                dataType: "json",
                type: "POST",
                data: {project: JSON.stringify(project)},
                timeout: 10000,
                complete: function () {
                    Kotlin.modules = originalKotlinModules;
                    instance.onComplete()
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        if(jqXHR.responseText != null && jqXHR.responseText != ""){
                            instance.onFail(jqXHR.responseText);
                        } else {
                            instance.onFail(textStatus + " : " + errorThrown);
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