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
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/30/12
 * Time: 3:37 PM
 */

var AccordionView = (function () {
    function AccordionView(/*Element*/element) {
        var DEFAULT_PROJECT_ID = "/Examples/Hello,%20world!/Simplest%20version";

        var instance = {
            loadAllContent: function () {
                element.innerHTML = "";
                projects = {};
                selectedProjectView = null;
                selectedFileView = null;
                headersProvider.getAllHeaders(function (folders) {
                        $(folders).each(function (ind, folderContent) {
                            if (folderContent.name == "My programs") {
                                myProgramsFolder = new Kotlin
                                    .modules["kotlin.web.demo.frontend"]
                                    .MyProgramsFolderView(element, folderContent, null, addProject);
                            } else if (folderContent.name == "Public links") {
                                publicLinksFolder = new Kotlin
                                    .modules["kotlin.web.demo.frontend"]
                                    .FolderView(element, folderContent, null, addProject);
                            } else {
                                new Kotlin
                                    .modules["kotlin.web.demo.frontend"]
                                    .FolderView(element, folderContent, null, addProject);
                            }
                        });
                        incompleteActionManager.checkTimepoint("headersLoaded");
                        $(element).accordion("refresh");
                        if (!loginView.isLoggedIn()) {
                            $(myProgramsFolder.headerElement).unbind("click");
                        }
                        loadFirstItem();
                    }
                );
            },
            addNewProject: function (name, publicId, fileId) {
                addProject(myProgramsFolder.contentElement, {
                    name: name,
                    publicId: publicId,
                    type: ProjectType.USER_PROJECT
                }, myProgramsFolder);
                projects[publicId].getProjectData().setDefaultContent();
                projects[publicId].getProjectData().addFileWithMain(name, fileId);
                selectProject(publicId);
            },
            addNewProjectWithContent: function (publicId, content) {
                addProject(myProgramsFolder.contentElement, {
                    name: content.name,
                    publicId: publicId,
                    type: ProjectType.USER_PROJECT
                }, myProgramsFolder);
                projects[publicId].getProjectData().setContent(content);
                selectProject(publicId);
            },
            onLogout: function () {
            },
            getSelectedProject: function () {
                return selectedProjectView.getProjectData();
            },
            getSelectedProjectView: function () {
                return selectedProjectView;
            },
            validateNewProjectName: function (name) {
                return myProgramsFolder.validateNewProjectName(name)
            },
            onBeforeUnload: function () {
                var publicLinks = [];
                for (var id in projects) {
                    if (projects[id].getType() == ProjectType.PUBLIC_LINK) {
                        publicLinks.push(projects[id].getHeader());
                    }
                }
                localStorage.setItem("publicLinks", JSON.stringify(publicLinks))
            },
            onProjectSelected: function (selectedProject) {

            },
            getSelectedFile: function () {
                if (selectedFileView != null) {
                    return selectedFileView.getFile();
                } else {
                    return null;
                }
            },
            getSelectedFileView: function () {
                return selectedFileView;
            },
            selectFile: function (fileView) {
                if (!(selectedFileView == fileView)) {
                    if (selectedProjectView == fileView.getProjectView()) {
                        var previousFileView = selectedFileView;
                        selectedFileView = fileView;

                        var previousFile = null;
                        if (previousFileView != null) {
                            $(previousFileView.getWrapper()).removeClass("selected");
                            previousFile = previousFileView.getFile();
                        }
                        $(selectedFileView.getWrapper()).addClass("selected");

                        instance.onSelectFile(previousFile, selectedFileView.getFile());
                    } else {
                        throw "You can't select file from project, that isn't selected";
                    }
                }
            },
            onSelectFile: function (previousFile, currentFile) {
            },
            selectedFileDeleted: function () {
                selectedFileView = null;
                instance.onSelectedFileDeleted();
            },
            onSelectedFileDeleted: function () {

            },
            onUnmodifiedSelectedFile: function () {
            },
            onModifiedSelectedFile: function () {
            },
            onProjectDeleted: function () {

            },
            loadFirstItem: function () {
                loadFirstItem();
            },
            selectProject: function (publicId) {
                selectProject(publicId)
            },
            getProjectView: function (publicId) {
                return projects[publicId];
            }
        };

        element.innerHTML = "";
        $(element).accordion({
            heightStyle: "content",
            navigation: true,
            active: 0,
            icons: {
                activeHeader: "examples-open-folder-icon",
                header: "examples-closed-folder-icon"
            }
        });
//        var programsView = new ProgramsView(programsModel);

        var projects = {};
        var selectedProjectView = null;
        var selectedFileView = null;
        var myProgramsFolder;
        var publicLinksFolder;

        function loadFirstItem() {
            var projectId = getProjectIdFromUrl();
            if (projectId == null || projectId == "") {
                if (localStorage.getItem("openedItemId") != null) {
                    projectId = localStorage.getItem("openedItemId");
                } else {
                    projectId = DEFAULT_PROJECT_ID;
                }
            }
            localStorage.removeItem("openedItemId");

            if (isUserProjectInUrl()) {
                if (localStorage.getItem(projectId) == null) {
                    var file_id = getFileIdFromUrl();
                    headersProvider.getHeaderByFilePublicId(file_id, projectId, function (header) {
                        if (!(header.publicId in projects)) {
                            if (header.type == ProjectType.PUBLIC_LINK) {
                                header.timeStamp = new Date().getTime();
                                addProject(publicLinksFolder.contentElement, header, publicLinksFolder);
                            } else {
                                throw "Project wasn't downloaded";
                            }
                        }
                        selectProject(header.publicId);
                    });
                } else {
                    selectProject(projectId);
                }
            } else if (projectId != "") {
                selectProject(projectId);
            }
        }

        function addProject(/*Element*/ folderContentElement, header, parent) {
            if (header.type == ProjectType.PUBLIC_LINK && projects[header.publicId] != null) {
                return
            } else if (projects[header.publicId] != null) {
                throw("Duplicate project id");
            }
            var projectHeaderElement = document.createElement("div");
            var projectContentElement = document.createElement("div");

            folderContentElement.appendChild(projectHeaderElement);
            folderContentElement.appendChild(projectContentElement);


            var projectView = new ProjectView(header, projectContentElement, projectHeaderElement, parent);
            projectView.onHeaderClick = selectProject;
            projectView.onDelete = function () {
                if (selectedProjectView == projects[header.publicId]) {
                    history.replaceState("", "", "index.html");
                    selectedProjectView = null;
                    selectedFileView = null;
                    loadFirstItem();
                }
                delete projects[header.publicId];
            };
            projectView.onSelected = function () {
                if (projectView.getProjectData().isEmpty()) {
                    selectedFileView = null;
                }
                instance.onProjectSelected(this.getProjectData());
            };

            projects[header.publicId] = projectView;

            return projectView;
        }

        function selectProject(publicId) {
            if (selectedProjectView == null || selectedProjectView.getProjectData().getPublicId() != publicId) {
                if (selectedProjectView != null) {
                    $(selectedProjectView.getHeaderElement()).removeClass("selected");
                    $(selectedProjectView.getContentElement()).slideUp();
                }
                selectedProjectView = projects[publicId];
                selectedProjectView.select();
            }
        }

        return instance;
    }


    return AccordionView;
})
();
