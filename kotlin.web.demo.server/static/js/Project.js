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
 * Created by Semyon.Atamas on 8/18/2014.
 */


var Project = (function () {
    function Project(url, element, content) {

        var selectedFile = null;
        var isProjectContentChanged = false;

        var instance = {
            onContentLoaded: function (data) {
                content = data;
                for (var i = 0; i < content.files.length; i++) {
                    content.files[i].errors = [];
                    content.files[i].save = function () {
                        this.content = editor.getText();
                        if (isUserProject()) {
                            projectProvider.saveFile(url, this);
                        } else {
                            localStorage.setItem(url, JSON.stringify(content));
                        }
                    };

                    content.files[i].onContentChange = function (text) {
                        isProjectContentChanged = true;
                        this.content = text;
                        actionsView.setStatus("unsavedChanges");
                    }

                }
                showProjectContent();
                if (content.files.length > 0) {
                    selectFile(content.files[0]);
                }
                instance.select();

            },
            rename: function (newName) {
                url = url.substring(0, url.indexOf("&name=") + "&name=".length) + newName;
                content.name = newName;
                showProjectContent();
                selectFile(selectedFile);
            },
            select: function () {
                argumentsView.change = function () {
                    content.args = argumentsView.val();
                };

                problemsView.onProjectChange(instance);
                helpViewForExamples.showHelp(content.help);
                if (selectedFile != null) {
                    editor.open(selectedFile);
                }
                argumentsView.val(content.args);
                configurationManager.updateConfiguration(content.confType);
                actionsView.refresh();
            },
            processHighlightingResult: function (data) {
                for (var i = 0; i < content.files.length; i++) {
                    content.files[i].errors = data[content.files[i].name];
                }
                editor.updateHighlighting();
            },
            getModifiableContent: function () {
                return {
                    args: content.args,
                    confType: content.confType,
                    name: content.name,
                    parent: content.parent,
                    originUrl: content.originUrl,
                    files: content.files.filter(function (file) {
                        return file.modifiable;
                    })
                };
            },
            restoreDefault: function () {
                projectProvider.loadExample(url);
            },
            getURL: function () {
                return url;
            },
            getName: function () {
                return content.name;
            },
            errorsExists: function () {
                for (var i = 0; i < content.files.length; i++) {
                    var errors = content.files[i].errors;
                    for (var j = 0; j < errors.length; j++) {
                        if (errors[j].severity == "ERROR") {
                            return true;
                        }
                    }
                }
                return false;
            },
            save: function () {
                if (isProjectContentChanged) {
                    if (isUserProject()) {

                        projectProvider.saveProject({
                            args: content.args,
                            confType: content.confType,
                            name: content.name,
                            parent: content.parent,
                            files: []
                        });
                        actionsView.setStatus("default");
                    } else {
                        localStorage.setItem(url, JSON.stringify(content));
                    }
                }
            },
            saveAs: function () {
                if (loginView.isLoggedIn()) {
                    saveProjectDialog.open(projectProvider.forkProject.bind(null, instance.getModifiableContent()))
                } else {
                    $("#login-dialog").dialog("open");
                }
            },
            changeConfiguration: function (confType) {
                isProjectContentChanged = true;
                content.confType = confType;
            },
            getFiles: function () {
                return content.files;
            },
            addNewFile: function (filename) {
                content.files.push({
                    name: filename,
                    content: "",
                    errors: "",
                    modifiable: "true",
                    type: "Kotlin",
                    save: function () {
                        this.content = editor.getText();
                        if (isUserProject()) {
                            projectProvider.saveFile(url, this);
                        } else {
                            localStorage.setItem(url, JSON.stringify(content));
                        }
                    },
                    onContentChange: function () {
                        isProjectContentChanged = true;
                        actionsView.setStatus("unsavedChanges");
                    }
                });
                problemsView.onProjectChange(instance);
                showProjectContent();
                selectFile(content.files[content.files.length - 1]);

            },
            onFail: function () {
            },
            onDelete: function () {
                element.parentNode.removeChild(element);
            },
            isUserProject: function () {
                return isUserProject();
            }
        };

        var actionsView = new ProjectActionsView(document.getElementById("editor-notifications"), instance);

        var projectProvider = new ProjectProvider(instance);
        projectProvider.onExampleLoaded = function (data) {
            isProjectContentChanged = false;
            localStorage.removeItem(url);
            actionsView.setStatus("default");
            instance.onContentLoaded(data);
        };

        projectProvider.onProjectSave = function() {
            isProjectContentChanged = false;
        };

        projectProvider.onProjectLoaded = function (data) {
            localStorage.removeItem(url);
            actionsView.setStatus("default");
            instance.onContentLoaded(data.content);
        };

        projectProvider.onDeleteProject = function () {

        };

        projectProvider.onDeleteFile = function (url) {
            onDeleteFile(url);
            problemsView.onProjectChange(instance);
        };

        projectProvider.onProjectFork = function (name) {
            var newContent = copy(content);
            newContent.name = name;
            newContent.parent = "My Programs";
            accordion.addNewProject(newContent.name, newContent);
        };

        projectProvider.onFileRenamed = function (url, newName) {
            newName = newName.endsWith(".kt") ? newName : newName + ".kt";
            renameFileHeader(url, newName);
            var oldName = url.substr(url.indexOf("&filename=") + "&filename=".length);
            for (var i = 0; i < content.files.length; ++i) {
                if (content.files[i].name == oldName) {
                    content.files[i].name = newName;
                    break;
                }
            }
        };

        var newFileDialog = new InputDialogView("Add new file", "Filename:", "Add");
        var saveProjectDialog = new InputDialogView("Save project", "Project name:", "Save");
        var renameFileDialog = new InputDialogView("Rename file", "filename", "Rename");

        (function loadProject() {
            if (content == null) {
                var localContent = JSON.parse(localStorage.getItem(url));
                if (localContent != null) {
                    instance.onContentLoaded(localContent);
                    actionsView.setStatus("unsavedChanges");
                } else {
                    if (isUserProject()) {
                        projectProvider.loadProject(url);
                    } else {
                        projectProvider.loadExample(url);
                    }
                }
            } else {
                instance.onContentLoaded(content);
            }
        })();

        function onDeleteFile(url) {
            var fileName = url.substr(url.indexOf("&filename=") + "&filename=".length);
            var id = -1;
            for (var i = 0; i < content.files.length; ++i) {
                if (content.files[i].name == fileName) {
                    id = i;
                    break;
                }
            }
            content.files.splice(id, 1);
            if (content.files.length != 0) {
                selectedFile = null;
                showProjectContent();
                if (id != 0) {
                    selectFile(content.files[id - 1]);
                } else {
                    selectFile(content.files[id]);
                }
            } else {
                accordion.deleteProject(instance.getURL());
            }
        }

        function selectFile(file) {
            if (selectedFile != null) {
                document.getElementById(getFilenameURL(selectedFile.name) + "_header").className = "example-filename";
            }
            selectedFile = file;
            document.getElementById(getFilenameURL(selectedFile.name) + "_header").className = "example-filename-selected";
            editor.open(selectedFile);
        }

        function getFilenameURL(filename) {
            return instance.getURL() + "&filename=" + filename.replace(/ /g, "_");
        }

        function isUserProject() {
            return url.indexOf("My_Programs") == 0;
        }


        function showProjectContent() {
            element.innerHTML = "";

            for (var i = 0; i < content.files.length; i++) {
                var file = content.files[i];

                var filenameDiv = document.createElement("div");
                filenameDiv.id = getFilenameURL(file.name) + "_header";
                filenameDiv.className = "example-filename";

                var icon = document.createElement("div");
                if (file.type == "Kotlin") {
                    icon.className = "kotlin-file-type-default"
                } else {
                    icon.className = "kotlin-file-type-default"
                }
                filenameDiv.appendChild(icon);


                var fileNameSpan = document.createElement("div");
                fileNameSpan.className = "example-filename-text";
                fileNameSpan.innerHTML = file.name;
                fileNameSpan.id = getFilenameURL(file.name);
                filenameDiv.appendChild(fileNameSpan);

                if (isUserProject() && file.modifiable) {
                    var renameImg = document.createElement("div");
                    renameImg.className = "rename-img";
                    renameImg.title = "Rename this file";
                    renameImg.onclick = (function (file) {
                        return function (event) {
                            renameFileDialog.open(projectProvider.renameFile.bind(null, getFilenameURL(file.name)));
                            event.stopPropagation();
                        }
                    })(file);

                    var deleteImg = document.createElement("div");
                    deleteImg.className = "delete-img";
                    deleteImg.title = "Delete this file";
                    deleteImg.onclick = (function (file) {
                        return function (event) {
                            projectProvider.deleteFile(getFilenameURL(file.name));
                            event.stopPropagation();
                        }
                    })(file);
                    filenameDiv.appendChild(deleteImg);
                    filenameDiv.appendChild(renameImg);
                }

                filenameDiv.onclick = (function (file) {
                    return function () {
                        selectFile(file);
                    }
                })(file);

                element.appendChild(filenameDiv);
            }

            if (isUserProject()) {
                var addFileButton = document.createElement("div");
                addFileButton.className = "example-filename";
                addFileButton.innerHTML = "Add new file";
                addFileButton.style.cursor = "pointer";
                addFileButton.onclick = newFileDialog.open.bind(null, projectProvider.addNewFile);
                element.appendChild(addFileButton);
            }
        }

        function renameFileHeader(url, newName) {

            var element = document.getElementById(url);
            element.innerHTML = newName;
            element.id = getFilenameURL(newName);
            element.parentNode.id = getFilenameURL(newName) + "_header";
        }

        return instance;
    }

    return Project;
})
();