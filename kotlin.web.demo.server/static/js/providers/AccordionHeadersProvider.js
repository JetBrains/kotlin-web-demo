/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
 * Created by Semyon.Atamas on 8/26/2014.
 */


var AccordionHeadersProvider = (function () {

    function AccordionHeadersProvider(onAllExamplesLoaded, onAllProgramsLoaded, onFail) {

        var instance = {
            getAllExamples: function () {
                getAllExamples();
            },
            getAllPrograms: function () {
                getAllPrograms();
            },
            addNewProject: function(name){
                addNewProject(name);
            },
            deleteProject: function (url) {
                deleteProject(url);
            }
        };

        function getAllExamples() {
            $.ajax({
                url: generateAjaxUrl("loadExampleHeaders", "all"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            onAllExamplesLoaded(data);
                        } else {
                            onFail(data, ActionStatusMessages.load_examples_fail);
                        }
                    } else {
                        onFail("Incorrect data format.", ActionStatusMessages.load_examples_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_examples_fail);
                }
            });
        }

        function getAllPrograms() {
            $.ajax({
                url: generateAjaxUrl("loadProject", "all"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            onAllProgramsLoaded(data);
                        } else {
                            onFail(data, ActionStatusMessages.load_programs_fail);
                        }
                    } else {
                        onFail("Incorrect data format.", ActionStatusMessages.load_programs_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_programs_fail);
                }
            });
        }

        function addNewProject(name){
            $.ajax({
                url: generateAjaxUrl("addProject", name),
                success: function(){
                    accordion.addNewProject(name);
                },
                type: "POST",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }

        function deleteProject(url) {
            $.ajax({
                url: generateAjaxUrl("deleteProject", url),
                success: function () {
                    accordion.deleteProject(url);
                },
                type: "POST",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }

        return instance;
    }

    return AccordionHeadersProvider;
})();