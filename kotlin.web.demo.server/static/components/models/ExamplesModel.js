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
    var instance;

    function ExamplesModel() {

        instance = {
            loadExample:function (url) {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("loadExample", url),
                    context:document.body,
                    success:function (data) {
                        instance.onLoadExample(true, data);
                    },
                    dataType:"json",
                    type:"GET",
                    timeout:10000,
                    error:function () {
                        instance.onLoadExample(false, null);
                    }
                });
            },
            getAllExamples:function () {
                getAllExamples();
            },
            onAllExamplesLoaded:function (status, data) {
            },
            onLoadExample:function (status, data) {
            }
        };

        return instance;
    }


    function getAllExamples() {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("loadExample", "all"),
            context:document.body,
            success:function (data) {
                instance.onAllExamplesLoaded(true, data);
            },
            dataType:"json",
            type:"GET",
            timeout:10000,
            error:function () {
                instance.onAllExamplesLoaded(false, null);
            }
        });
    }

    return ExamplesModel;
})();