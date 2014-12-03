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
                    dumpToLocalStorage();
                }
            },
            onChange: function () {

            },
            loadContent: function () {
                if (localStorage.getItem(publicId) != null) {
                    var content = JSON.parse(localStorage.getItem(publicId));
                    for (var i = 0; i < content.files.length; ++i) {
                        content.files[i] = File.fromLocalStorage(instance, content.files[i]);
                    }
                    onContentLoaded(content)
                } else {
                    projectProvider.loadProject(publicId, type, function (content) {
                        for (var i = 0; i < content.files.length; ++i) {
                            content.files[i] = new File(instance, content.files[i]);
                        }
                        onContentLoaded(content);
                    });
                }
            },
            loadOriginal: function () {
                instance.loadContent();
            },
            onContentLoaded: function (files) {
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
            getContentCopy: function () {
                return copy({
                    files: files,
                    args: args,
                    confType: confType,
                    originUrl: originUrl,
                    help: help,
                    type: type
                });
            },
            setArguments: function (args) {
                instance.args = args;
                instance.onChange();
            },
            getPublicId: function () {
                return publicId
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
                instance.onChange();
            },
            getHelp: function () {
                return help;
            },
            setErrors: function (errors) {
                for (var i = 0; i < files.length; i++) {
                    files[i].setErrors(errors[files[i].getName()]);
                }
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

        function save() {
            if (type != ProjectType.USER_PROJECT) throw "You can't save non-user projects";

            projectProvider.saveProject(instance, publicId, function () {
                modified = false;
            })
        }

        function dumpToLocalStorage() {
            if (type == ProjectType.USER_PROJECT) throw "User project shouldn't be saved in local storage";
            var fileIDs = [];
            for (var i = 0; i < files.length; ++i) {
                fileIDs.push(files.getPublicId());
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
                publicId: publicId
            }));
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
            instance.onContentLoaded(files);
        }

        return instance;
    }

    return ProjectData;
})();

