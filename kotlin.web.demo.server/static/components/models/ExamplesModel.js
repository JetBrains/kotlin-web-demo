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
 write_exception
 load_example
 get_all_examples
 */

var ExamplesModel = (function () {
    var eventHandler = new EventsHandler();

    function ExamplesModel() {

        var instance = {
            addListener:function (name, f) {
                eventHandler.addListener(name, f);
            },
            fire:function (name, param) {
                eventHandler.fire(name, param);
            },
            loadExample:function (url) {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("loadExample", url),
                    context:document.body,
                    success:function (data) {
                        if (data != null && typeof data != "undefined") {
                            if (data[0] != null && typeof data[0] != "undefined"
                                && typeof data[0].exception != "undefined") {
                                eventHandler.fire("write_exception", data);
                            } else {
                                var result = new Example();
                                result.name = getNameByUrl(url);
                                result.text = data[0].text;
                                result.args = data[0].args;
                                //TODO
                                result.runner = substringDependencies(data[0].runner);
                                result.dependencies = data[0].dependencies;
                                result.defaultDependencies = substringDependencies(data[0].dependencies);
                                eventHandler.fire("load_example", true, result);
                            }
                        } else {
                            eventHandler.fire("write_exception", "Received data is null.");
                        }

                    },
                    dataType:"json",
                    type:"GET",
                    timeout:10000,
                    error:function () {
                        eventHandler.fire("load_example", false, null);
                    }
                });
            },
            getAllExamples:function () {
                getAllExamples();
            }
        };

        return instance;
    }

    function substringDependencies(dependencies) {
        var pos = dependencies.indexOf(" ");
        if (pos >= 0) {
            return dependencies.substring(0, pos);
        }
        return dependencies;
    }


    function getAllExamples() {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("loadExample", "all"),
            context:document.body,
            success:function (data) {
                eventHandler.fire("get_all_examples", true, data);
            },
            dataType:"json",
            type:"GET",
            timeout:10000,
            error:function () {
                eventHandler.fire("get_all_examples", false, null);
            }
        });
    }

    return ExamplesModel;
})();