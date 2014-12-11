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
 * Created by Semyon.Atamas on 9/25/2014.
 */

var ProjectType = {
    EXAMPLE: "EXAMPLE",
    USER_PROJECT: "USER_PROJECT",
    PUBLIC_LINK: "PUBLIC_LINK"
};

var ProjectView = (function () {

    function ProjectView(header, /*Element*/ contentElement, /*Element*/ headerElement) {

        var instance = {
            getHeader: function () {
                return header;
            },
            saveAs: function () {
                if (loginView.isLoggedIn()) {
                    function onProjectFork(publicId, name) {
                        var newContent = copy(project);
                        newContent.name = name;
                        newContent.parent = "My Programs";
                        accordion.addNewProject(newContent.name, publicId, null, newContent);
                        projectProvider.loadProject(publicId, header.type, createProject);
                    }
                } else {
                    loginDialog.dialog("open");
                }
            },
            setSelectedFileView: function (selectedFileView_) {
                selectedFileView = selectedFileView_;
            },
            select: function () {
                headerElement.className += " selected";
                headerElement.parentNode.previousSibling.click();
                $(contentElement).slideDown();
                if (!project.isContentLoaded()) {
                    project.loadContent(false);
                } else {
                    if (selectedFileView != null) {
                        selectedFileView.fireSelectEvent();
                    } else{
                        editor.closeFile();
                    }
                    instance.onSelected(instance);
                }

                if (header.type == ProjectType.PUBLIC_LINK) {
                    header.timeStamp = new Date().getTime();
                }
            },
            getType: function () {
                return header.type;
            },
            getFileViewByName: function (name) {
                for (var fileId in fileViews) {
                    if (fileViews[fileId].getFile().getName() == name) {
                        return fileViews[fileId];
                    }
                }
            },
            validateNewFileName: function (fileName) {
                fileName = addKotlinExtension(fileName);
                for (var i = 0; i < project.getFiles().length; i++) {
                    if (project.getFiles()[i].getName() == fileName) {
                        return {valid: false, message: "File with this name already exists in the project"};
                    }
                }
                return {valid: true};
            },
            getHeaderElement: function () {
                return headerElement;
            },
            getContentElement: function () {
                return contentElement;
            },
            getProjectData: function () {
                return project;
            },
            onHeaderClick: function (publicId) {

            },
            onSelected: function () {

            },
            onDelete: function () {

            }
        };

        var nameSpan;
        var modified = false;
        var project = (function () {

            project = new ProjectData(header.type, header.publicId, header.name);

            project.onRenamed = function (newName) {
                nameSpan.innerHTML = newName;
            };

            project.onContentLoaded = function () {
                var files = project.getFiles();
                contentElement.innerHTML = "";

                for (var i = 0; i < files.length; ++i) {
                    var fileView;
                    fileView = createFileView(files[i]);
                    fileViews[files[i].getPublicId()] = fileView;
                }

                if (files.length > 0) {
                    selectedFileView = fileViews[files[0].getPublicId()];
                    if (accordion.getSelectedProject().getPublicId() == project.getPublicId()) {
                        selectedFileView.fireSelectEvent();
                        instance.onSelected(instance);
                    }
                } else if(accordion.getSelectedProject().getPublicId() == project.getPublicId()){
                    editor.closeFile();
                }
            };

            project.onFileAdded = function (file) {
                var fileView = createFileView(file);
                fileViews[file.getPublicId()] = fileView;
                selectedFileView = fileView;
                if (isSelected()) {
                    fileView.fireSelectEvent();
                }
            };

            project.onFileDeleted = function (publicId) {
                delete fileViews[publicId];
                if (selectedFileView.getFile().getPublicId() == publicId && project.files.length > 0) {
                    selectedFile = null;
                    contentElement.firstChild.click()
                }
            };

            return project;
        })();
        var selectedFile = null;
        var selectedFileView = null;
        var fileViews = {};
        var renameProjectDialog = new InputDialogView("Rename project", "Project name:", "Rename");
        renameProjectDialog.validate = function (newName) {
            if (header.name == newName) {
                return {valid: true};
            } else {
                return accordion.validateNewProjectName(newName);
            }
        };
        var newFileDialog = new InputDialogView("Add new file", "File name:", "Add");
        newFileDialog.validate = instance.validateNewFileName;


        init();
        function init() {
            $(contentElement).slideUp();
            headerElement.className = "examples-project-name";
            var img = document.createElement("div");
            img.className = "arrow";
            headerElement.appendChild(img);
            headerElement.onclick = function () {
                instance.onHeaderClick(header.publicId);
            };

            nameSpan = document.createElement("span");
            nameSpan.className = "file-name-span";
            nameSpan.style.cursor = "pointer";
            nameSpan.innerHTML = header.name;
            headerElement.appendChild(nameSpan);

            if (header.type == ProjectType.USER_PROJECT || header.type == ProjectType.PUBLIC_LINK) {
                var deleteButton = document.createElement("div");
                deleteButton.className = "delete-img";
                deleteButton.title = "Delete this project";
                deleteButton.onclick = function (event) {
                    if (confirm("Delete project " + header.name + "?")) {
                        projectProvider.deleteProject(header.publicId, header.type, function () {
                            headerElement.parentNode.removeChild(headerElement);
                            contentElement.parentNode.removeChild(contentElement);
                            instance.onDelete();
                        });
                    }
                    event.stopPropagation();
                };
                headerElement.appendChild(deleteButton);
            }

            if (header.type == ProjectType.USER_PROJECT) {
                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename this file";
                renameImg.onclick = function (event) {
                    event.stopPropagation();
                    renameProjectDialog.open(projectProvider.renameProject.bind(null, project), project.getName());
                };
                headerElement.appendChild(renameImg);

                var addFileImg = document.createElement("div");
                addFileImg.className = "new-file-button";
                addFileImg.innerHTML = "Add new file";
                addFileImg.style.cursor = "pointer";
                addFileImg.onclick = function (event) {
                    event.stopPropagation();
                    newFileDialog.open(fileProvider.addNewFile.bind(null, project), "Untitled");
                };
                headerElement.appendChild(addFileImg);
            }

        }

        function createFileView(file) {
            var fileHeader = document.createElement("div");
            contentElement.appendChild(fileHeader);
            return new FileView(instance, fileHeader, file);
        }

        function isSelected() {
            return accordion.getSelectedProject() == project;
        }

        return instance;
    }


    return ProjectView;
})();