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

    function AccordionHeadersProvider(onAllExamplesLoaded, onAllProgramsLoaded) {

        var instance = {
            getAllExamples: function () {
                getAllExamples();
            },
            getAllPrograms: function () {
                getAllPrograms();
            },
            addNewProject: function (name) {
                addNewProject(name);
            },
            deleteProject: function (url) {
                deleteProject(url);
            },
            renameProject: function (publicId, newName) {
                renameProject(publicId, newName);
            },
            getInfoAboutProject: function (publicId) {
                getInfoAboutProject(publicId);
            },
            onRenameProject: function (url, newName) {

            },
            onFail: function () {
            },
            onProjectInfoLoaded: function (data) {

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
                            instance.onFail(data, statusBarView.statusMessages.load_examples_fail);
                        }
                    } else {
                        instance.onFail("Incorrect data format.", statusBarView.statusMessages.load_examples_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.load_examples_fail);
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
                            instance.onFail(data, statusBarView.statusMessages.load_programs_fail);
                        }
                    } else {
                        instance.onFail("Incorrect data format.", statusBarView.statusMessages.load_programs_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.load_programs_fail);
                }
            });
        }

        function addNewProject(name) {
            $.ajax({
                url: generateAjaxUrl("addProject", name),
                success: function (data) {
                    accordion.addNewProject(name, data.projectId, data.fileId, null);
                },
                type: "POST",
                timeout: 10000,
                dataType: 'json',
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        function renameProject(publicId, newName) {
            $.ajax({
                url: generateAjaxUrl("renameProject"),
                success: function () {
                    instance.onRenameProject(publicId, newName);
                },
                type: "POST",
                data: {publicId: publicId, newName: newName},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        function deleteProject(publicId) {
            $.ajax({
                url: generateAjaxUrl("deleteProject"),
                success: function () {
                    accordion.deleteProject(publicId);
                },
                type: "POST",
                data: {publicId: publicId},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        function getInfoAboutProject(publicId) {
            $.ajax({
                url: generateAjaxUrl("loadProjectInfoByFileId"),
                success: function (data) {
                    instance.onProjectInfoLoaded(data);
                },
                type: "GET",
                timeout: 10000,
                dataType: "json",
                data: {
                    id: publicId
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        return instance;
    }

    return AccordionHeadersProvider;
})();