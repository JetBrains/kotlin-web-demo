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

var FileProvider = (function () {

    function FileProvider() {
        var instance = {
            addNewFile: function (project, fileName) {
                addNewFile(project, fileName);
            },
            renameFile: function (publicId, callback, newName) {
                renameFile(publicId, callback, newName);
            },
            saveFile: function (file, callback) {
                saveFile(file, callback);
            },
            deleteFile: function (publicId, callback) {
                deleteFile(publicId, callback);
            },
            loadOriginalFile: function (file, onSuccess, onNotFound) {
                loadOriginalFile(file, onSuccess, onNotFound);
            },
            onNewFileAdded: function () {
            },
            onOriginalFileLoaded: function () {
            },
            onDeleteFile: function () {
            },
            onFileRenamed: function (newName) {
            },
            onFileSaved: function () {
            },
            onFail: function (message, status) {
            }
        };

        function loadOriginalFile(file, onSuccess, onNotFound) {
            if (file.getProjectType() == ProjectType.EXAMPLE) {
                blockContent();
                $.ajax({
                    url: generateAjaxUrl("loadExampleFile"),
                    success: function (data) {
                        try {
                            unBlockContent();
                            onSuccess(data);
                            instance.onOriginalFileLoaded();
                        } catch (e) {
                            console.log(e);
                        }
                    },
                    type: "POST",
                    timeout: 10000,
                    data: {publicId: file.getPublicId()},
                    dataType: "json",
                    error: function (jqXHR, textStatus, errorThrown) {
                        try {
                            instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                        } catch (e) {
                            console.log(e)
                        }
                    },
                    complete: unBlockContent
                })
            } else if (file.getProjectType() == ProjectType.PUBLIC_LINK) {
                blockContent();
                $.ajax({
                    url: generateAjaxUrl("loadProjectFile"),
                    success: function (data) {
                        try {
                            unBlockContent();
                            onSuccess(data);
                            instance.onOriginalFileLoaded();
                        } catch (e) {
                            console.log(e);
                        }
                    },
                    type: "POST",
                    timeout: 10000,
                    data: {publicId: file.getPublicId()},
                    dataType: "json",
                    statusCode: {
                        404: onNotFound
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        try {
                            instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                        } catch (e) {
                            console.log(e)
                        }
                    },
                    complete: unBlockContent
                })
            } else {
                throw "User files can't be reloaded from server.";
            }
        }

        function addNewFile(project, filename) {
            blockContent();
            filename = addKotlinExtension(filename);
            $.ajax({
                url: generateAjaxUrl("addFile"),
                success: function (publicId) {
                    try {
                        instance.onNewFileAdded(filename);
                        project.addEmptyFile(filename, publicId);
                    } catch (e) {
                        console.log(e)
                    }
                },
                type: "POST",
                timeout: 10000,
                data: {publicId: project.getPublicId(), filename: filename},
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e) {
                        unBlockContent();
                    }
                },
                complete: unBlockContent
            })
        }

        function renameFile(publicId, callback, newName) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("renameFile"),
                success: function () {
                    try {
                        instance.onFileRenamed(newName);
                        callback(newName);
                    } catch (e) {
                        console.log(e)
                    }
                },
                type: "POST",
                timeout: 10000,
                data: {publicId: publicId,
                    newName: newName},
                error: function (jqXHR, textStatus, errorThrown) {
                    try {
                        instance.onFail(textStatus, errorThrown);
                    } catch (e) {
                        console.log(e);
                    }
                },
                complete: unBlockContent
            })
        }

        function deleteFile(publicId, callback) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("deleteFile"),
                context: document.body,
                success: function () {
                    try {
                        instance.onDeleteFile();
                        callback();
                    } catch (e) {
                        console.log(e);
                    }
                },
                type: "POST",
                data: {publicId: publicId},
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

        function saveFile(file, callback) {
            blockContent();
            $.ajax({
                url: generateAjaxUrl("saveFile"),
                type: "POST",
                timeout: 10000,
                success: function () {
                    try {
                        instance.onFileSaved();
                        callback();
                    } catch (e) {
                        console.log(e)
                    }
                },
                data: {file: JSON.stringify(file)},
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

        return instance;
    }

    return FileProvider;
})();