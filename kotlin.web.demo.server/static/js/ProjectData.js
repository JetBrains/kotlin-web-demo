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
 * Created by Semyon.Atamas on 10/13/2014.
 */

var ProjectData = (function () {

    function ProjectData(type, publicId, name) {
        var instance = {
            toJSON: function () {
                return {
                    name: name,
                    parent: parent,
                    args: args,
                    confType: confType,
                    originUrl: originUrl,
                    readOnlyFileNames: readOnlyFileNames,
                    files: files.filter(function (file) {
                        return file.isModifiable();
                    })
                };
            },
            isContentLoaded: function () {
                return contentLoaded
            },
            rename: function (newName) {
                name = newName;
                instance.onRenamed(newName);
            },
            onRenamed: function (newName) {
            },
            hasErrors: function () {
                for (var i = 0; i < files.length; i++) {
                    var errors = files[i].getErrors();
                    for (var j = 0; j < errors.length; j++) {
                        if (errors[j].severity == "ERROR") {
                            return true;
                        }
                    }
                }
                return false;
            },
            save: function () {
                if (instance.getType() == ProjectType.USER_PROJECT) {
                    save();
                } else {
                    updateProjectInLocalStorage();
                }
            },
            loadContent: function (fromServer) {
                if (localStorage.getItem(publicId) != null && !fromServer) {
                    var content = JSON.parse(localStorage.getItem(publicId));
                    localStorage.removeItem(publicId);
                    for (var i = 0; i < content.files.length; ++i) {
                        content.files[i] = File.fromLocalStorage(instance, content.files[i]);
                    }
                    onContentLoaded(content)
                } else {
                    projectProvider.loadProject(
                        publicId,
                        type,
                        function (content) {
                            for (var i = 0; i < content.files.length; ++i) {
                                content.files[i] = new File(instance, content.files[i]);
                            }
                            onContentLoaded(content);
                        },
                        instance.onContentNotFound
                    )
                }
            },
            loadOriginal: function () {
                instance.loadContent(true);
            },
            onContentLoaded: function (files) {
            },
            onContentNotFound: function () {

            },
            addEmptyFile: function (name, publicId) {
                var file = File.EmptyFile(instance, name, publicId);
                files.push(file);
                instance.onFileAdded(file);
            },
            onFileAdded: function () {
            },
            deleteFile: function (publicId) {
                files = files.filter(function (element) {
                    return element.getPublicId() != publicId;
                });
            },
            onFileDeleted: function (publicId) {

            },
            setContent: function (content) {
                if (!contentLoaded) {
                    for (var i = 0; i < content.files.length; ++i) {
                        content.files[i] = new File(instance, content.files[i]);
                    }
                    onContentLoaded(content);
                    contentLoaded = true;
                } else {
                    throw "Content was already loaded";
                }
            },
            setDefaultContent: function () {
                if (!contentLoaded) {
                    contentLoaded = true;
                } else {
                    throw "Content was already loaded";
                }
            },

            getName: function () {
                return name;
            },
            getArgs: function () {
                return args;
            },
            setArguments: function (newArgs) {
                args = newArgs;
            },
            getPublicId: function () {
                return publicId;
            },
            getType: function () {
                return type
            },
            getFiles: function () {
                return files
            },
            getConfiguration: function () {
                return confType;
            },
            setConfiguration: function (newConfType) {
                confType = newConfType;
            },
            getHelp: function () {
                return help;
            },
            setErrors: function (errors) {
                for (var i = 0; i < files.length; i++) {
                    files[i].setErrors(errors[files[i].getName()]);
                }
            },
            isEmpty: function () {
                return files.length == 0;
            },
            isRevertible: function () {
                return revertible;
            },
            makeNotRevertible: function () {
                revertible = false
            }
        };

        var files = [];
        var contentLoaded = false;
        var args = "";
        var confType = "java";
        var originUrl = null;
        var parent = "My programs";
        var help = "";
        var modified = false;
        var revertible = true;
        var readOnlyFileNames = [];

        function save() {
            if (type != ProjectType.USER_PROJECT) throw "You can't save non-user projects";

            projectProvider.saveProject(instance, publicId, function () {
                modified = false;
            })
        }

        function updateProjectInLocalStorage() {
            if (type == ProjectType.USER_PROJECT) throw "User project shouldn't be saved in local storage";
            if (isModified()) {
                var fileIDs = [];
                for (var i = 0; i < files.length; ++i) {
                    files[i].dumpToLocalStorage();
                    fileIDs.push(files[i].getPublicId());
                }
                localStorage.setItem(publicId, JSON.stringify({
                    name: name,
                    files: fileIDs,
                    args: args,
                    confType: confType,
                    originUrl: originUrl,
                    parent: parent,
                    help: help,
                    type: type,
                    publicId: publicId,
                    revertible: revertible,
                    readOnlyFileNames: readOnlyFileNames
                }));
            } else {
                localStorage.removeItem(publicId);
            }
        }

        function isModified() {
            for (var i = 0; i < files.length; ++i) {
                if (files[i].isModified()) {
                    return true;
                }
            }
            return false;
        }

        function onContentLoaded(content) {
            contentLoaded = true;
            originUrl = content.originUrl;
            args = content.args;
            name = content.name;
            confType = content.confType;
            parent = content.parent;
            help = content.help;
            files = content.files;
            revertible = content.hasOwnProperty("revertible") ? content.revertible : true;
            readOnlyFileNames = content.readOnlyFileNames;
            instance.onContentLoaded(files);
        }

        return instance;
    }

    return ProjectData;
})();

