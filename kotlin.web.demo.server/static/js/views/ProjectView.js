/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

    function ProjectView(header, /*Element*/ contentElement, /*Element*/ headerElement, parent) {

        var instance = {
            getHeader: function () {
                return header;
            },
            setSelectedFileView: function (selectedFileView_) {
                selectedFileView = selectedFileView_;
            },
            selectFileFromUrl: function () {
                selectedFileView = getFileFromUrl();
                if (selectedFileView != null) {
                    selectedFileView.fireSelectEvent();
                }
            },
            select: function () {
                parent.select();
                headerElement.className += " selected";
                $(contentElement).slideDown();
                if (!project.isContentLoaded()) {
                    project.loadContent(false);
                } else {
                    if (selectedFileView != null) {
                        selectedFileView.fireSelectEvent();
                    } else {
                        if (!project.isEmpty()) {
                            selectFirstFile();
                        }
                    }
                    instance.onSelected(instance);
                }

                if (header.type == ProjectType.PUBLIC_LINK) {
                    header.timeStamp = new Date().getTime();
                }
            },
            updateFileViewSafely: function (fileView, newName) {
                if (fileView.getHeaderText() == newName) {
                    return;
                }
                var validationResult = instance.validateNewFileName(newName);
                if (!validationResult.valid) {
                    instance.updateFileViewSafely(
                        validationResult.collidedFileView,
                        addKotlinExtension(removeKotlinExtension(newName) + "'")
                    );
                }
                fileView.file.name = newName;
                fileView.updateName();
            },
            getType: function () {
                return header.type;
            },
            getFileViewByName: function (name) {
                for (var fileId in fileViews) {
                    if (fileViews[fileId].file.name == name) {
                        return fileViews[fileId];
                    }
                }
            },
            validateNewFileName: function (fileName) {
                if(fileName == ""){
                    return {valid: false, message: "File name can't be empty"};
                }
                if(fileName.length >= 95){
                    return {valid: false, message: "File name is too long"}
                }
                if(!(/^[a-zA-Z0-9,_\- ]+$/).test(fileName)){
                    return {valid: false, message: "File name can contain only the following characters:" +
                    "<span style=\"font-family: monospace\"> a-z A-Z 0-9 ' ' ',' '_' '-'</span>"};
                }
                fileName = addKotlinExtension(fileName);
                for (var i in fileViews) {
                    if (fileViews[i].getHeaderText() == fileName) {
                        return {
                            valid: false,
                            message: "File with this name already exists in the project",
                            collidedFileView: fileViews[i]
                        };
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
            getDepth: function(){
                return depth;
            },
            onHeaderClick: function (publicId) {

            },
            onSelected: function () {

            },
            onDelete: function () {

            }
        };

        var nameSpan;
        var depth =  parseInt(parent.headerElement.getAttribute("depth")) + 1;
        var project = (function () {

            project = new ProjectData(header.type, header.publicId, header.name, parent);

            project.onModified(function (isProjectContentChanged) {
                if (isProjectContentChanged) {
                    $(headerElement).addClass("modified");
                } else {
                    $(headerElement).removeClass("modified");
                }
            });

            project.onRenamed = function (newName) {
                nameSpan.innerHTML = newName;
                nameSpan.title = nameSpan.innerHTML;
                if(isSelected()){
                    navBarView.onSelectedProjectRenamed(newName);
                }
            };

            project.onContentLoaded = function () {
                var files = project.getFiles();
                fileViews = {};
                contentElement.innerHTML = "";

                nameSpan.innerHTML = project.getName();
                nameSpan.title = nameSpan.innerHTML;

                for (var i = 0; i < files.length; ++i) {
                    var fileView;
                    fileView = createFileView(files[i]);
                    fileViews[files[i].id] = fileView;
                }

                if (files.length > 0) {
                    selectedFileView = getFileFromUrl();

                    if (selectedFileView == null) {
                        selectedFileView = fileViews[files[0].id];
                    }

                    if (accordion.getSelectedProject().getPublicId() == project.getPublicId()) {
                        selectedFileView.fireSelectEvent();
                        instance.onSelected(instance);
                    }
                } else if (accordion.getSelectedProject().getPublicId() == project.getPublicId()) {
                    instance.onSelected(instance);
                    editor.closeFile();
                }
            };

            project.onContentNotFound = function () {
                if (project.getType() == ProjectType.PUBLIC_LINK) {
                    window.alert("Can't find project origin, maybe it was removed by the user.");
                    project.makeNotRevertible();
                    if (!project.isContentLoaded()) {
                        onDelete();
                    }
                }
            };

            project.onFileAdded = function (file) {
                var fileView = createFileView(file);
                fileViews[file.id] = fileView;
                selectedFileView = fileView;
                if (isSelected()) {
                    fileView.fireSelectEvent();
                }
            };

            project.onFileDeleted = function (publicId) {
                if (selectedFileView.file.id == publicId) {
                    accordion.selectedFileDeleted();
                    selectedFileView = null;
                }
                delete fileViews[publicId];
                if (!project.isEmpty()) {
                    selectFirstFile();
                }
            };

            return project;
        })();
        var selectedFileView = null;
        var fileViews = {};



        init();
        function init() {
            headerElement.id = header.publicId;

            $(contentElement).slideUp();
            headerElement.className = "examples-project-name";
            headerElement.setAttribute("depth", depth.toString());
            var img = document.createElement("div");
            img.className = "icon";
            headerElement.appendChild(img);
            headerElement.onclick = function () {
                instance.onHeaderClick(header.publicId);
            };

            nameSpan = document.createElement("span");
            nameSpan.className = "file-name-span";
            nameSpan.style.cursor = "pointer";
            nameSpan.innerHTML = header.name;
            nameSpan.title = nameSpan.innerHTML;
            headerElement.appendChild(nameSpan);

            var actionIconsElement = document.createElement("div");
            actionIconsElement.className = "icons";
            headerElement.appendChild(actionIconsElement);

            if (header.type == ProjectType.USER_PROJECT) {
                var addFileImg = document.createElement("div");
                addFileImg.className = "new-file icon";
                addFileImg.onclick = function (event) {
                    event.stopPropagation();
                    Kotlin.modules["kotlin.web.demo.frontend"].views.dialogs.InputDialogView.open(
                        "Add new file",
                        "File name:",
                        "Add",
                        "Untitled",
                        instance.validateNewFileName,
                        fileProvider.addNewFile.bind(null, project)
                    );
                };
                actionIconsElement.appendChild(addFileImg);

                var renameImg = document.createElement("div");
                renameImg.className = "rename icon";
                renameImg.title = "Rename this project";
                renameImg.onclick = function (event) {
                    event.stopPropagation();
                    Kotlin.modules["kotlin.web.demo.frontend"].views.dialogs.InputDialogView.open(
                        "Rename project",
                        "Project name:",
                        "Rename",
                        project.getName(),
                        function (newName) {
                            if (project.getName() == newName) {
                                return {valid: true};
                            } else {
                                return accordion.validateNewProjectName(newName);
                            }
                        },
                        projectProvider.renameProject.bind(null, project)

                    );
                };
                actionIconsElement.appendChild(renameImg);
            }

            if (header.type == ProjectType.USER_PROJECT || header.type == ProjectType.PUBLIC_LINK) {
                var deleteButton = document.createElement("div");
                deleteButton.className = "delete icon";
                deleteButton.title = "Delete this project";
                deleteButton.onclick = function (event) {
                    if (confirm("Delete project " + header.name + "?")) {
                        projectProvider.deleteProject(header.publicId, header.type, onDelete);
                    }
                    event.stopPropagation();
                };
                actionIconsElement.appendChild(deleteButton);
            }

            if (header.type != ProjectType.USER_PROJECT) {
                if (localStorage.getItem(header.publicId) != null) {
                    $(headerElement).addClass("modified");
                }

                var revertIcon = document.createElement("div");
                revertIcon.className = "revert icon";
                revertIcon.title = "Revert this project";
                actionIconsElement.appendChild(revertIcon);

                revertIcon.onclick = function () {
                    project.loadOriginal();
                };

                project.onNotRevertible = function(){
                    revertIcon.parentNode.removeChild(revertIcon);
                }
            }
        }

        function getFileFromUrl() {
            var fileId = getFileIdFromUrl();
            if (fileViews.hasOwnProperty(fileId)) {
                return fileViews[fileId]
            } else {
                return instance.getFileViewByName(getFileIdFromUrl());
            }
        }

        function selectFirstFile() {
            selectedFileView = fileViews[project.getFiles()[0].id];
            selectedFileView.fireSelectEvent();
        }

        function createFileView(file) {
            return new Kotlin.modules["kotlin.web.demo.frontend"].views.FileView(instance, contentElement, file);
        }

        function isSelected() {
            return accordion.getSelectedProject() == project;
        }

        function onDelete() {
            if(project.getType() == ProjectType.USER_PROJECT)
                parent.removeProject(instance);
            headerElement.parentNode.removeChild(headerElement);
            contentElement.parentNode.removeChild(contentElement);
            instance.onDelete();
        }

        return instance;
    }


    return ProjectView;
})();