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
 help_for_examples
 help_for_words
 */

var HelpModel = (function () {
    var instance;
    function HelpModel() {

        instance = {
            getAllHelpForExamples:function () {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("loadHelpForExamples", "null"),
                    context:document.body,
                    success:function (data) {
                        instance.onHelpForExamplesLoaded(true, data);
                    },
                    dataType:"json",
                    type:"GET",
                    timeout:30000,
                    error:function () {
                        instance.onHelpForExamplesLoaded(false, null);
                    }
                });
            },
            getAllHelpForWords:function () {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("loadHelpForWords", "null"),
                    context:document.body,
                    success:function (data) {
                        instance.onHelpForWordsLoaded(true, data);
                    },
                    dataType:"json",
                    type:"GET",
                    timeout:30000,
                    error:function () {
                        instance.onHelpForWordsLoaded(false, null);
                    }
                });
            },
            onHelpForExamplesLoaded:function (status, data) {

            },
            onHelpForWordsLoaded:function (status, data) {

            }
        };

        return instance;
    }

    return HelpModel;
})();