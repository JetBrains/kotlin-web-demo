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
            renameProject: function (project, newName) {
                renameProject(project, newName);
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
            saveProject: function (project, publicId, callback) {
                saveProject(project, publicId, callback);
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


        function renameProject(project, newName) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("renameProject"),
                success: function () {
                    try {
                        instance.onProjectRenamed(newName);
                        project.rename(newName);
                    } catch (e) {
                        console.log(e);
                    }
                },
                type: "POST",
                data: {publicId: project.getPublicId(), newName: newName},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        unBlockContent();
                        instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            })
        }

        function loadExample(publicId, callback) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("loadExample"),
                context: document.body,
                success: function (data) {
                    try {
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
                    } catch (e) {
                        console.log(e)
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                data: {publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_example_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            });
        }

        function addNewProject(name) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("addProject"),
                success: function (data) {
                    try {
                        instance.onNewProjectAdded(name, data.projectId, data.fileId);
                    } catch (e) {
                        console.log(e);
                    }
                },
                type: "POST",
                timeout: 10000,
                data: {args: name},
                dataType: 'json',
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            })
        }

        function loadProject(publicId, callback) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("loadProject"),
                context: document.body,
                success: function (data) {
                    try {
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
                    } catch (e) {
                        console.log(e)
                    }
                },
                dataType: "json",
                data: {publicId: publicId},
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            });
        }


        function forkProject(content, callback, name) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("addProject"),
                success: function (data) {
                    try {
                        instance.onProjectForked(name);
                        callback(data);
                    } catch (e) {
                        console.log(e);
                    }
                },
                type: "POST",
                timeout: 10000,
                dataType: "json",
                data: {content: JSON.stringify(content), args: name},
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            })
        }

        function checkIfProjectExists(publicId, onSuccess, onFail) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("checkIfProjectExists"),
                success: function (flag) {
                    try {
                        if (flag == "true") {
                            onSuccess()
                        } else {
                            onFail();
                        }
                    } catch (e) {
                        console.log(e)
                    }
                },
                type: "POST",
                timeout: 10000,
                data: {publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            })
        }

        function deleteProject(publicId, callback) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("deleteProject"),
                success: function () {
                    try {
                        instance.onProjectDeleted();
                        callback();
                    } catch (e) {
                        console.log(e)
                    }
                },
                type: "POST",
                data: {publicId: publicId},
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                    } catch (e) {
                        console.log(e)
                    }
                },
                complete: unBlockContent
            })
        }

        function saveProject(project, publicId, callback) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("saveProject"),
                type: "POST",
                success: function () {
                    try {
                        instance.onProjectSaved();
                        callback();
                    } catch (e) {
                        console.log(e);
                    }
                },
                timeout: 10000,
                data: {project: JSON.stringify(project, publicId), publicId: publicId},
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e) {
                        console.log(e);
                    }
                },
                complete: unBlockContent
            })
        }


        return instance;
    }

    return ProjectProvider;
})();