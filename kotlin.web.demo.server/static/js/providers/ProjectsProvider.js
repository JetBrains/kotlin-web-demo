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

    function ProjectProvider(project) {
        var instance = {
            loadProject: function (publicId, type, callback) {
                if (type == ProjectType.EXAMPLE) {
                    loadExample(publicId, callback);
                } else {
                    loadProject(publicId, callback);
                }
            },
            renameProject: function (publicId, callback, newName) {
                renameProject(publicId, callback, newName);
            },
            deleteProject: function (id, type, callback) {
                if (type == ProjectType.USER_PROJECT) {
                    deleteProject(id, callback);
                } else if (type == ProjectType.PUBLIC_LINK) {
                    callback();
                } else {
                    throw ("Can't delete this project");
                }
            },
            addNewProject: function (name) {
                addNewProject(name);
            },
            forkProject: function (content, callback, name) {
                forkProject(content, callback, name)
            },
            saveProject: function (content, publicId, callback) {
                saveProject(content, publicId);
            },
            onProjectLoaded: function (projectContent) {
            },
            onProjectForked: function () {

            },
            onProjectSaved: function () {

            },
            checkIfProjectExists: function (publicId, onSuccess, onFail) {
                checkIfProjectExists(publicId, onSuccess, onFail);
            },
            onNewProjectAdded: function (name, projectId, fileId) {

            },
            onProjectDeleted: function (publicId) {

            },
            onProjectRenamed: function () {

            },
            onFail: function () {

            }
        };


        function renameProject(publicId, callback, newName) {
            $.ajax({
                url: generateAjaxUrl("renameProject"),
                success: function () {
                    instance.onProjectRenamed(newName);
                    callback(newName);
                },
                type: "POST",
                data: {publicId: publicId, newName: newName},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        function loadExample(publicId, callback) {
            $.ajax({
                url: generateAjaxUrl("loadExample"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            instance.onProjectLoaded(data);
                            callback(data);
                        } else {
                            instance.onFail(data, ActionStatusMessages.load_example_fail);
                        }
                    } else {
                        instance.onFail("Incorrect data format.", ActionStatusMessages.load_example_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                data: {publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_example_fail);
                }
            });
        }

        function addNewProject(name) {
            $.ajax({
                url: generateAjaxUrl("addProject"),
                success: function (data) {
                    instance.onNewProjectAdded(name, data.projectId, data.fileId);
                },
                type: "POST",
                timeout: 10000,
                data: {args: name},
                dataType: 'json',
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        function loadProject(publicId, callback) {
            $.ajax({
                url: generateAjaxUrl("loadProject"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            instance.onProjectLoaded(data);
                            callback(data)
                        } else {
                            instance.onFail(data, ActionStatusMessages.load_program_fail);
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
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
                }
            });
        }


        function forkProject(content, callback, name) {
            $.ajax({
                url: generateAjaxUrl("addProject"),
                success: function (publicId) {
                    instance.onProjectFork(name, publicId);
                    callback(name, publicId);
                },
                type: "POST",
                timeout: 10000,
                data: {content: JSON.stringify(content), args: name},
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
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

        function deleteProject(publicId, callback) {
            $.ajax({
                url: generateAjaxUrl("deleteProject"),
                success: function () {
                    instance.onProjectDeleted();
                    callback();
                },
                type: "POST",
                data: {publicId: publicId},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        function saveProject(content, publicId) {
            $.ajax({
                url: generateAjaxUrl("saveProject"),
                type: "POST",
                success: function () {
                    instance.onProjectSaved();
                    callback();
                },
                timeout: 10000,
                data: {project: JSON.stringify(content), publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
            })
        }


        return instance;
    }

    return ProjectProvider;
})();