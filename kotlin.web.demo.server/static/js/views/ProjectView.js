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
    EXAMPLE: "example",
    USER_PROJECT: "user",
    PUBLIC_LINK: "public"
};

var ProjectView = (function () {

    function ProjectView(header, /*Element*/ contentElement, /*Element*/ headerElement) {

        var instance = {
            deselect: function () {
                selected = false;
                if (selectedFile != null) {
                    selectedFile.deselect();
                }
                $(headerElement).removeClass("selected");
                $(contentElement).slideUp();
            },
            getName: function () {
                return header.name;
            },
            saveAs: function () {
                saveProjectDialog.open(projectProvider.forkProject.bind(null, project, onProjectFork), header.name);
            },
            select: function () {
                selected = true;
                if (project == null) {
                    projectProvider.loadProject(header.publicId, header.type, createProject);
                } else if (selectedFile != null) {
                    problemsView.onProjectChange();
                    selectedFile.select();
                }
                headerElement.className += " selected";
                headerElement.parentNode.previousSibling.click();
                $(contentElement).slideDown();
            },
            createProject: function (content) {
                createProject(content);
            },
            getType: function () {
                return header.type;
            },
            onHeaderClick: function (publicId) {

            },
            verifyNewFilename: function (fileName) {
                fileName = fileName.endsWith(".kt") ? fileName : fileName + ".kt";
                for (var i = 0; i < project.files.length; i++) {
                    if (project.files[i].name == fileName) {
                        return false;
                    }
                }
                return true;
            },
            getProjectData: function () {
                return project;
            },
            getModifiableContent: function () {
                return project.getModifiableContent();
            },
            processHighlightingResult: function (errors) {
                project.processHighlightingResult(errors);
            },
            onDelete: function () {

            }
        };

        var nameSpan;
        var selected = false;
        var project = null;
        var selectedFile = null;
        var fileViews = {};
        var renameProjectDialog = new InputDialogView("Rename project", "Project name:", "Rename");
        renameProjectDialog.verify = accordion.verifyNewProjectname;
        var newFileDialog = new InputDialogView("Add new file", "Filename:", "Add");
        newFileDialog.verify = instance.verifyNewFilename;
        var saveProjectDialog = new InputDialogView("Save project", "Project name:", "Save");
        saveProjectDialog.verify = accordion.verifyNewProjectname;
        init();

        function createProject(content) {
            contentElement.innerHTML = "";
            if (header.type == ProjectType.USER_PROJECT) {
                addFileButton();
            }

            project = new ProjectData(content);

            var filesContent = content.files;
            var fileView;
            if (filesContent.length > 0) {
                fileView = createFileView(filesContent[0].publicId, filesContent[0].name, filesContent[0]);
                fileViews[filesContent[0].publicId] = fileView;
                project.files.push(fileView.getFileData());
                selectFile(filesContent[0].publicId)
            }

            for (var i = 1; i < filesContent.length; ++i) {
                var fileContent = filesContent[i];
                fileView = createFileView(fileContent.publicId, fileContent.name, fileContent);
                fileViews[fileContent.publicId] = fileView;
                project.files.push(fileView.getFileData());
            }

            problemsView.onProjectChange();
        }

        function onProjectRenamed(newName) {
            header.name = newName;
            if (project != null) {
                project.name = newName;
            }
            nameSpan.innerHTML = newName;
        }

        function onDelete() {
            instance.deselect();
            headerElement.parentNode.removeChild(headerElement);
            contentElement.parentNode.removeChild(contentElement);
            instance.onDelete();
        }

        function onNewFileAdded(publicId, name) {
            fileViews[publicId] = createFileView(publicId, name);
            selectFile(publicId);
        }

        function onProjectFork(publicId, name) {
            var newContent = copy(project);
            newContent.name = name;
            newContent.parent = "My Programs";
            accordion.addNewProject(newContent.name, publicId, null, newContent);
            projectProvider.loadProject(publicId, header.type, createProject);
        }

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

            if (header.type == ProjectType.USER_PROJECT) {
                var deleteButton = document.createElement("div");
                deleteButton.className = "delete-img";
                deleteButton.title = "Delete this project";
                deleteButton.onclick = function (event) {
                    if (confirm("Delete project " + header.name + "?")) {
                        projectProvider.deleteProject(header.publicId, onDelete);
                    }
                    event.stopPropagation();
                };
                headerElement.appendChild(deleteButton);

                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename this file";
                renameImg.onclick = function (event) {
                    renameProjectDialog.open(projectProvider.renameProject.bind(null, header.publicId, onProjectRenamed), header.name);
                    event.stopPropagation();

                };
                headerElement.appendChild(renameImg);
            }

        }

        function addFileButton() {
            var button = document.createElement("div");
            button.className = "example-filename";
            button.innerHTML = "Add new file";
            button.style.cursor = "pointer";
            button.onclick = function () {
                newFileDialog.open(fileProvider.addNewFile.bind(null, header.publicId, onNewFileAdded), "File");
            };
            contentElement.appendChild(button);
        }

        function createFileView(publicId, name, /*nullable*/fileContent) {
            var fileHeader = document.createElement("div");
            if (header.type == ProjectType.USER_PROJECT) {
                contentElement.insertBefore(fileHeader, contentElement.lastChild);
            } else {
                contentElement.appendChild(fileHeader);
            }
            var fileView = new FileView(instance, name, publicId, fileHeader, fileContent);

            fileView.canBeSelected = function () {
                return selected;
            };

            fileView.onDelete = function (publicId) {
                project.files = project.files.filter(function (element) {
                    return element.publicId != publicId;
                });
                delete fileViews[publicId];

                if (selectedFile.getPublicId() == publicId && project.files.length > 0) {
                    contentElement.firstChild.click()
                }
            };

            fileView.onHeaderClick = selectFile;
            return fileView;
        }

        function selectFile(publicId) {
            if (selectedFile != null) {
                selectedFile.deselect();
            }
            selectedFile = fileViews[publicId];
            selectedFile.select();
        }

        return instance;
    }


    return ProjectView;
})();