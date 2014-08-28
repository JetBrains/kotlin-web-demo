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
        var content = null;
        var selectedFile = null;

        var instance = {
            onContentLoaded: function (data) {
                content = data;
                for (var i = 0; i < content.files.length; i++) {
                    content.files[i].errors = [];
                    content.files[i].save = function(){
                        this.content = editor.getText();
                        if(loginView.isLoggedIn()) {
                            projectProvider.saveFile(url, this);
                        }
                    }
                }
                showProjectContent();
                if (content.files.length > 0) {
                    selectFile(content.files[0]);
                }
                if(!content.isLocalVersion && loginView.isLoggedIn()){
                    projectProvider.addNewProjectFromExample(this.getModifiableContent());
                }
            },
            select: function () {
                problemsView.onExampleChange();
                helpViewForExamples.showHelp(content.help);
                if (selectedFile != null) {
                    editor.open(selectedFile);
                }
                argumentsView.val(content.args);
                configurationManager.updateConfiguration(content.confType);
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
                    files: content.files.filter(function (file) {
                        return file.modifiable;
                    })
                };
            },
            selectFile: function (id) {
                selectFile(content.files[id]);
            },
            getArguments: function () {
                return content.args;
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
                content.confType = configurationManager.getType();
                content.args = argumentsView.val();
            },
            getFiles: function () {
                return content.files;
            },
            addNewFile: function (filename) {
                content.files.push({
                    name: filename,
                    content: "",
                    errors: "",
                    type: "Kotlin"
                });
                selectedFile = content.files[content.files.length - 1];
                showProjectContent();
            },
            onDeleteFile: function (id) {
                content.files.splice(id, 1);
                showProjectContent();
            },
            onFail: function () {
            },
            isUserProject: function(){
                isUserProject();
            }
        };

        var projectProvider = new ProjectProvider(instance);
        var newFileDialog = new InputDialogView("Add new file", "Filename:", "Add", projectProvider.addNewFile);

        if(content == null) {
            if (isUserProject() || loginView.isLoggedIn()) {
                projectProvider.loadProject(url);
            } else {
                projectProvider.loadExample(url);
            }
        }


        function selectFile(file) {
            if (selectedFile != null) {
                document.getElementById(getFilenameURL(selectedFile.name)).className = "example-filename";
            }
            selectedFile = file;
            document.getElementById(getFilenameURL(selectedFile.name)).className = "example-filename-selected";
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
                filenameDiv.id = getFilenameURL(file.name);
                filenameDiv.className = "example-filename";

                var icon = document.createElement("div");
                if (file.type == "Kotlin") {
                    icon.className = "kotlin-file-type-default"
                } else {
                    icon.className = "kotlin-file-type-default"
                }
                filenameDiv.appendChild(icon);


                var fileNameSpan = document.createElement("div");
                fileNameSpan.style.float = "left";
                fileNameSpan.innerHTML = file.name;
                filenameDiv.appendChild(fileNameSpan);

                if (isUserProject()) {
                    var deleteImg = document.createElement("div");
                    deleteImg.className = "delete-img";
                    deleteImg.title = "Delete this file";
                    deleteImg.onclick = (function (url, id) {
                        return function () {
                            projectProvider.deleteFile(url, id);
                        }
                    })(getFilenameURL(file.name), i);
                    filenameDiv.appendChild(deleteImg);
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
                addFileButton.onclick = newFileDialog.open;
                element.appendChild(addFileButton);
            }
        }

        return instance;
    }

    return Project;
})
();