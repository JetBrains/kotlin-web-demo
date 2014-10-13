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


var ProjectType = {
    EXAMPLE: "example",
    USER_PROJECT: "user",
    PUBLIC_LINK: "public"
};

var Project = (function () {

    function Project(url, element, projectContent, type) {
        if (type != ProjectType.EXAMPLE) {
            var publicId = url.substring("project_".length);
        }

        var selectedFile = null;
        var isProjectContentChanged = false;
        var selected = true;
        var ifDatabaseCopyExist = false;

        var instance = {
            onContentLoaded: function (data) {
                element.innerHTML = "";
                projectContent = data;
                for (var i = 0; i < projectContent.files.length; i++) {
                    var fileData = projectContent.files[i];
                    var fileHeader = document.createElement("div");
                    element.appendChild(fileHeader);
                    projectContent.files[i] = new File(instance, fileData.name, fileData.content, fileData.modifiable, fileData.publicId, fileHeader);
                }
                if (type != ProjectType.EXAMPLE) {
                    element.appendChild(createAddFileButton());
                }

                if (projectContent.files.length > 0) {
                    selectFile(projectContent.files[0]);
                }

                if(selected) {
                    instance.select();
                }
            },
            rename: function (newName) {
                projectContent.name = newName;
            },
            deselect: function(){
                selected = false;
            },
            select: function () {
                selected = true;
                argumentsView.change = function () {
                    projectContent.args = argumentsView.val();
                };

                problemsView.onProjectChange(instance);
                helpViewForExamples.showHelp(projectContent.help);
                if (selectedFile != null) {
                    selectedFile.open();
                } else{
                    editor.closeFile();
                }
                argumentsView.val(projectContent.args);
                configurationManager.updateConfiguration(projectContent.confType);
                actionsView.refresh();
            },
            selectFile: function(file){
                selectFile(file)
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
                if (type == ProjectType.EXAMPLE) {
                    projectProvider.loadExample(url);
                } else {
                    projectProvider.loadProject(publicId);
                }
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
                    if (type == ProjectType.USER_PROJECT) {
                        selectedFile.save();
                        projectProvider.saveProject({
                            args: projectContent.args,
                            confType: projectContent.confType,
                            name: projectContent.name,
                            parent: projectContent.parent,
                            files: []
                        }, publicId);
                        actionsView.setStatus("default");
                    } else {
                        localStorage.setItem(url, JSON.stringify(projectContent));
                    }
                }
            },
            saveAs: function () {
                if (loginView.isLoggedIn()) {
                    saveProjectDialog.open(forkProject, instance.getName())
                } else {
                    localStorage.setItem(url, JSON.stringify(projectContent));
                    localStorage.setItem("incompleteAction", "save");
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
            addNewFile: function (filename, publicId) {
                var fileHeader = document.createElement("div");
                element.insertBefore(fileHeader, element.lastChild);
                projectContent.files.push(new File(instance, filename, "", true, publicId, fileHeader));
                problemsView.onProjectChange(instance);
//                showProjectContent();
                selectFile(projectContent.files[projectContent.files.length - 1]);

            },
            onChange: function(){
                if (!isProjectContentChanged) {
                    isProjectContentChanged = true;
                    actionsView.setStatus("unsavedChanges");
                }
            },
            onFail: function () {
            },
            onDelete: function () {
                element.parentNode.removeChild(element);
            },
            onDeleteFile: function (file) {
                onDeleteFile(file)
            },
            getType: function () {
                return type;
            },
            verifyNewFilename: function(fileName){
                fileName = fileName.endsWith(".kt") ? fileName : fileName + ".kt";
                for(var i = 0; i< projectContent.files.length; i++){
                    if(projectContent.files[i].name == fileName){
                        return false;
                    }
                }
                return true;
            },
            checkIfDatabaseCopyExists: function (onSuccess, onFail) {
                projectProvider.checkIfProjectExists(publicId, onSuccess, onFail)
            }
        };

        var newFileDialog = new InputDialogView("Add new file", "Filename:", "Add");
        newFileDialog.verify = instance.verifyNewFilename;
        var saveProjectDialog = new InputDialogView("Save project", "Project name:", "Save");
        saveProjectDialog.verify = accordion.verifyNewProjectname;

        var actionsView = new ProjectActionsView(document.getElementById("editor-notifications"), instance);

        var projectProvider = new ProjectProvider(instance);

        projectProvider.onProjectSave = function () {
            isProjectContentChanged = false;
        };

        projectProvider.onProjectLoaded = function (data) {
            isProjectContentChanged = false;
            localStorage.removeItem(url);
            actionsView.setStatus("default");
            instance.onContentLoaded(data);
        };

        projectProvider.onDeleteProject = function () {

        };

        projectProvider.onDeleteFile = function (url) {
            onDeleteFile(url);
            problemsView.onProjectChange(instance);
        };

        projectProvider.onProjectFork = function (name, publicId) {
            var newContent = copy(projectContent);
            newContent.name = name;
            newContent.parent = "My Programs";
            accordion.addNewProject(newContent.name, publicId, null, newContent);
            instance.restoreDefault();
        };

        projectProvider.onExistenceChecked = function (flag) {
            ifDatabaseCopyExist = flag;
        };


        (function loadProject() {
            if (projectContent == null) {
                var localContent = JSON.parse(localStorage.getItem(url));
                if (localContent != null) {
                    instance.onContentLoaded(localContent);
                    actionsView.setStatus("unsavedChanges");
                } else if (type == ProjectType.EXAMPLE) {
                    projectProvider.loadExample(url);
                } else {
                    projectProvider.loadProject(publicId);
                }
            } else {
                instance.onContentLoaded(projectContent);
            }
        })();

        function onDeleteFile(file) {
            var id = -1;
            for (var i = 0; i < projectContent.files.length; ++i) {
                if (projectContent.files[i] == file) {
                    id = i;
                    break;
                }
            }
            projectContent.files.splice(id, 1);

            if (projectContent.files.length == 0) {
                selectedFile = null;
                editor.closeFile();
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

            if(selected) {
                selectedFile.open();
                if (file.modifiable) {
                    actionsView.show();
                } else {
                    actionsView.hide();
                }
            }
        }

        function createAddFileButton() {
            var addFileButton = document.createElement("div");
            addFileButton.className = "example-filename";
            addFileButton.innerHTML = "Add new file";
            addFileButton.style.cursor = "pointer";
            addFileButton.onclick = function(){
                newFileDialog.open(projectProvider.addNewFile.bind(null, publicId), "File");
            };
            return addFileButton;
        }

        function forkProject(newProjectName){
            projectProvider.forkProject(instance.getModifiableContent(), newProjectName)
        }

        return instance;
    }

    return Project;
})
();