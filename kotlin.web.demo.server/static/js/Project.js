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
    function Project(url, element, projectContent) {


        var selectedFile = null;
        var isProjectContentChanged = false;

        var instance = {
            onContentLoaded: function (data) {
                projectContent = data;
                for (var i = 0; i < projectContent.files.length; i++) {
                    var fileData = projectContent.files[i];
                    var fileHeader = document.createElement("div");
                    element.appendChild(fileHeader);
                    projectContent.files[i] = new File(fileData.name, fileData.content, fileData.modifiable, fileHeader);
                }
                if (isUserProject()) {
                    element.appendChild(createAddFileButton());
                }
                if (projectContent.files.length > 0) {
                    selectFile(projectContent.files[0]);
                }
                instance.select();
            },
            rename: function (newName) {
                url = url.substring(0, url.indexOf("&name=") + "&name=".length) + newName;
                projectContent.name = newName;
                for (var i = 0; i < projectContent.files.length; i++) {
                    projectContent.files[i].onProjectRenamed();
                }
//                selectFile(selectedFile);
            },
            select: function () {
                argumentsView.change = function () {
                    projectContent.args = argumentsView.val();
                };

                problemsView.onProjectChange(instance);
                helpViewForExamples.showHelp(projectContent.help);
                if (selectedFile != null) {
                    editor.open(selectedFile);
                }
                argumentsView.val(projectContent.args);
                configurationManager.updateConfiguration(projectContent.confType);
                actionsView.refresh();
            },
            processHighlightingResult: function (data) {
                for (var i = 0; i < projectContent.files.length; i++) {
                    projectContent.files[i].errors = data[projectContent.files[i].name];
                }
                editor.updateHighlighting();
            },
            getModifiableContent: function () {
                return {
                    args: projectContent.args,
                    confType: projectContent.confType,
                    name: projectContent.name,
                    parent: projectContent.parent,
                    originUrl: projectContent.originUrl,
                    files: projectContent.files.filter(function (file) {
                        return file.modifiable;
                    })
                };
            },
            restoreDefault: function () {
                projectProvider.loadExample(url);
            },
            getUrl: function () {
                return url;
            },
            getName: function () {
                return projectContent.name;
            },
            errorsExists: function () {
                for (var i = 0; i < projectContent.files.length; i++) {
                    var errors = projectContent.files[i].errors;
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
                            args: projectContent.args,
                            confType: projectContent.confType,
                            name: projectContent.name,
                            parent: projectContent.parent,
                            files: []
                        });
                        actionsView.setStatus("default");
                    } else {
                        localStorage.setItem(url, JSON.stringify(projectContent));
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
                projectContent.confType = confType;
            },
            getFiles: function () {
                return projectContent.files;
            },
            addNewFile: function (filename) {
                var fileHeader = document.createElement("div");
                element.insertBefore(fileHeader, element.lastChild);
                projectContent.files.push(new File(filename, "", true, fileHeader));
                problemsView.onProjectChange(instance);
//                showProjectContent();
                selectFile(projectContent.files[projectContent.files.length - 1]);

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

        var File = (function (_super) {
            function File(name, content, modifiable, element) {
                var fileNameElement;

                var instance = {
                    name: name,
                    content: content,
                    modifiable: modifiable,
                    errors: [],
                    type: "Kotlin",
                    getUrl: function () {
                        return getUrl();
                    },
                    save: function () {
                        instance.content = editor.getText();
                        if (isUserProject()) {
                            projectProvider.saveFile(url, this);
                        } else {
                            _super.save();
                        }
                    },
                    onProjectRenamed: function () {
                        element.id = getUrl();
                    },
                    onContentChange: function (text) {
                        isProjectContentChanged = true;
                        instance.content = text;
                        actionsView.setStatus("unsavedChanges");
                    },
                    onDelete: function () {
                        element.parentElement.removeChild(element);
                    },
                    rename: function (newName) {
                        instance.name = newName;
                        element.id = getUrl();
                        fileNameElement.innerHTML = newName;
                    }
                };

                if (element != null) {

                    element.id = getUrl();
                    element.className = "example-filename";

                    var icon = document.createElement("div");
                    if (instance.type == "Kotlin") {
                        icon.className = "kotlin-file-type-default"
                    } else {
                        icon.className = "kotlin-file-type-default"
                    }
                    element.appendChild(icon);


                    fileNameElement = document.createElement("div");
                    fileNameElement.className = "example-filename-text";
                    fileNameElement.innerHTML = instance.name;
                    element.appendChild(fileNameElement);

                    if (isUserProject() && instance.modifiable) {
                        var renameImg = document.createElement("div");
                        renameImg.className = "rename-img";
                        renameImg.title = "Rename this file";
                        renameImg.onclick = function (event) {
                            renameFileDialog.open(projectProvider.renameFile.bind(null, getUrl()));
                            event.stopPropagation();
                        };


                        var deleteImg = document.createElement("div");
                        deleteImg.className = "delete-img";
                        deleteImg.title = "Delete this file";
                        deleteImg.onclick = function (event) {
                            projectProvider.deleteFile(instance.getUrl());
                            event.stopPropagation();
                        };
                        element.appendChild(deleteImg);
                        element.appendChild(renameImg);
                    }

                    element.onclick = function () {
                        selectFile(instance);
                    };
                }

                function getUrl() {
                    return _super.getUrl() + "&filename=" + instance.name;
                }

                return instance;
            }

            return File;
        })(instance);

        var actionsView = new ProjectActionsView(document.getElementById("editor-notifications"), instance);

        var projectProvider = new ProjectProvider(instance);
        projectProvider.onExampleLoaded = function (data) {
            isProjectContentChanged = false;
            localStorage.removeItem(url);
            actionsView.setStatus("default");
            instance.onContentLoaded(data);
        };

        projectProvider.onProjectSave = function () {
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
            var newContent = copy(projectContent);
            newContent.name = name;
            newContent.parent = "My Programs";
            accordion.addNewProject(newContent.name, newContent);
        };

        projectProvider.onFileRenamed = function (url, newName) {
            newName = newName.endsWith(".kt") ? newName : newName + ".kt";
            for (var i = 0; i < projectContent.files.length; ++i) {
                if (projectContent.files[i].getUrl() == url) {
                    projectContent.files[i].rename(newName);
                    break;
                }
            }
            problemsView.onProjectChange(instance);
        };

        var newFileDialog = new InputDialogView("Add new file", "Filename:", "Add");
        var saveProjectDialog = new InputDialogView("Save project", "Project name:", "Save");
        var renameFileDialog = new InputDialogView("Rename file", "filename", "Rename");

        (function loadProject() {
            if (projectContent == null) {
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
                instance.onContentLoaded(projectContent);
            }
        })();

        function onDeleteFile(url) {
            var fileName = url.substr(url.indexOf("&filename=") + "&filename=".length);
            var id = -1;
            for (var i = 0; i < projectContent.files.length; ++i) {
                if (projectContent.files[i].name == fileName) {
                    var file = projectContent.files[i];
                    id = i;
                    break;
                }
            }
            file.onDelete();
            projectContent.files.splice(id, 1);
            if (projectContent.files.length == 0) {
                accordion.deleteProject(instance.getUrl());
            } else if (selectedFile == file) {
                selectedFile = null;
                selectFile(projectContent.files[0]);
            }
        }

        function selectFile(file) {
            if (selectedFile != null) {
                document.getElementById(selectedFile.getUrl()).className = "example-filename";
            }
            selectedFile = file;
            document.getElementById(selectedFile.getUrl()).className = "example-filename-selected";
            editor.open(selectedFile);
        }

        function isUserProject() {
            return url.indexOf("My_Programs") == 0;
        }

        function createAddFileButton() {
            var addFileButton = document.createElement("div");
            addFileButton.className = "example-filename";
            addFileButton.innerHTML = "Add new file";
            addFileButton.style.cursor = "pointer";
            addFileButton.onclick = newFileDialog.open.bind(null, projectProvider.addNewFile);
            return addFileButton;
        }

        return instance;
    }

    return Project;
})
();