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


var ProjectProvider = (function () {

    function ProjectsProvider(project) {
        var instance = {
            loadExample: function (url) {
                loadExample(url)
            },
            loadProject: function (publicId) {
                loadProject(publicId)
            },
//            deleteProject: function (publicId) {
//                deleteProject(publicId);
//            },
            addNewFile: function (publicId, fileName) {
                addNewFile(publicId, fileName);
            },
            forkProject: function (content, url) {
                forkProject(content, url)
            },
            saveProject: function (content, publicId) {
                saveProject(content, publicId);
            },
            onProjectLoaded: function (data) {

            },
            onDeleteProject: function () {

            },
            onProjectFork: function () {

            },
            onProjectSave: function () {

            },
            checkIfProjectExists: function (publicId, onSuccess, onFail) {
                checkIfProjectExists(publicId, onSuccess, onFail);
            }
        };

        function loadExample(url) {
            $.ajax({
                url: generateAjaxUrl("loadExample", url),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            instance.onProjectLoaded(data);
                        } else {
                            project.onFail(data, ActionStatusMessages.load_example_fail);
                        }
                    } else {
                        project.onFail("Incorrect data format.", ActionStatusMessages.load_example_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_example_fail);
                }
            });
        }

        function loadProject(publicId) {
            $.ajax({
                url: generateAjaxUrl("loadProject"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            instance.onProjectLoaded(data);
                        } else {
                            project.onFail(data, ActionStatusMessages.load_program_fail);
                        }
                    } else {
                        project.onFail("Incorrect data format.", ActionStatusMessages.load_program_fail);
                    }
                },
                dataType: "json",
                data: {publicId: publicId},
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
                }
            });
        }


        function forkProject(content, name) {
            $.ajax({
                url: generateAjaxUrl("addProject", name),
                success: function (publicId) {
                    instance.onProjectFork(name, publicId);
                },
                type: "POST",
                timeout: 10000,
                data: {content: JSON.stringify(content)},
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }

        function addNewFile(projectPublicId, filename) {
            if (!filename.endsWith(".kt")) {
                filename = filename + ".kt";
            }
            $.ajax({
                url: generateAjaxUrl("addFile"),
                success: function (data) {
                    project.addNewFile(filename, data);
                },
                type: "POST",
                timeout: 10000,
                data: {publicId: projectPublicId, filename: filename},
                error: function (jqXHR, textStatus, errorThrown) {
                    project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }

        function checkIfProjectExists(publicId, onSuccess, onFail) {
            $.ajax({
                url: generateAjaxUrl("checkIfProjectExists"),
                success: function (flag) {
                    if (flag == "true") {
                        onSuccess()
                    } else {
                        onFail();
                    }
                },
                type: "POST",
                timeout: 10000,
                data: {publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }

//        function deleteProject(publicId) {
//            $.ajax({
//                url: generateAjaxUrl("deleteProject"),
//                context: document.body,
//                success: function () {
//                    instance.onDeleteProject(publicId);
//                },
//                type: "POST",
//                data:{publicId: publicId},
//                timeout: 10000,
//                error: function (jqXHR, textStatus, errorThrown) {
//                    project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
//                }
//            });
//        }

        function saveProject(content, publicId) {
            $.ajax({
                url: generateAjaxUrl("saveProject"),
                type: "POST",
                success: instance.onProjectSave,
                timeout: 10000,
                data: {project: JSON.stringify(content), publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }


        return instance;
    }

    return ProjectsProvider;
})();