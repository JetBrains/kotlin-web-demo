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
        var DEFAULT_PROJECT_ID = "Hello,%20world!/Simplest%20version";

        var instance = {
            loadAllContent: function () {
                element.innerHTML = "";
                projects = {};
                selectedProjectView = null;
                selectedFileView = null;
                headersProvider.getAllHeaders(function (folders) {
                        $(folders).each(function (ind, folder) {
                            if (folder.name == "My programs") {
                                addMyProjectsFolder(folder.projects);
                            } else if (folder.name == "Public links") {
                                publicLinksContentElement = addFolder(folder);
                            } else {
                                addFolder(folder)
                            }
                        });
                        incompleteActionManager.checkTimepoint("headersLoaded");
                        $(element).accordion("refresh");
                        if (!loginView.isLoggedIn()) {
                            $(myProgramsHeaderElement).unbind("click");
                        }
                        loadFirstItem();
                    }
                );
            },
            addNewProject: function (name, publicId, fileId) {
                addProject(myProgramsContentElement, {name: name, publicId: publicId, type: ProjectType.USER_PROJECT}, "My programs");
                projects[publicId].getProjectData().setDefaultContent();
                projects[publicId].getProjectData().addFileWithMain(name, fileId);
                selectProject(publicId);
            },
            addNewProjectWithContent: function (publicId, content) {
                addProject(myProgramsContentElement, {
                    name: content.name,
                    publicId: publicId,
                    type: ProjectType.USER_PROJECT
                }, "My programs");
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
            validateNewProjectName: function (projectName) {
                if(projectName == ""){
                    return {valid: false, message: "Project name can't be empty"};
                }
                if(!(/^[a-zA-Z0-9,_\- ]+$/).test(projectName)){
                    return {valid: false, message: "Project name can contain only the following characters:" +
                    "<span style=\"font-family: monospace\"> a-z A-Z 0-9 ' ' ',' '_' '-'</span>"};
                }
                for (var url in projects) {
                    var project = projects[url].getProjectData();
                    if (project.getName() == projectName &&
                        project.getType() == ProjectType.USER_PROJECT) {
                        return {valid: false, message: "Project with that name already exists"};
                    }
                }
                return {valid: true};
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
                            $(previousFileView.getHeaderElement()).removeClass("selected");
                            previousFile = previousFileView.getFile();
                        }
                        $(selectedFileView.getHeaderElement()).addClass("selected");

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

        var myProgramsContentElement;
        var myProgramsHeaderElement;
        var publicLinksContentElement;
        var newProjectDialog = new InputDialogView("Add new project", "Project name:", "Add");
        newProjectDialog.validate = instance.validateNewProjectName;

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
                                addProject(publicLinksContentElement, header, "Public links");
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
        }

        function addFolder(folder) {
            var headerElement = document.createElement("h3");
            headerElement.className = "examples-folder-name depth-0";
            headerElement.id = escapeString(folder.name);
            element.appendChild(headerElement);

            var folderDiv = document.createElement("div");
            folderDiv.innerHTML = folder.name;
            folderDiv.className = "folder-name-div";
            headerElement.appendChild(folderDiv);

            var cont = document.createElement("div");
            element.appendChild(cont);

            $(folder.projects).each(function (ind, project) {
                addProject(cont, project, folder.name);
            });
            return cont;
        }

        function addMyProjectsFolder(projects) {
            myProgramsHeaderElement = document.createElement("h3");
            myProgramsHeaderElement.className = "examples-folder-name depth-0";
            myProgramsHeaderElement.id = escapeString("My programs");
            element.appendChild(myProgramsHeaderElement);

            var folderDiv = document.createElement("div");
            folderDiv.innerHTML = "My programs";
            folderDiv.className = "folder-name-div";
            myProgramsHeaderElement.appendChild(folderDiv);


            myProgramsContentElement = document.createElement("div");
            myProgramsContentElement.id = "My_Programs_content";
            element.appendChild(myProgramsContentElement);

            if (!loginView.isLoggedIn()) {
                folderDiv.style.display = "inline-block";
                myProgramsHeaderElement.style.color = "rgba(0,0,0,0.5)";
                var login_link = document.createElement("span");
                login_link.id = "login-link";
                login_link.className = "login-link";
                login_link.innerHTML = "(please log in)";
                myProgramsHeaderElement.onclick = function (event) {
                    loginView.openLoginDialog();
                    event.stopPropagation()
                };
                myProgramsHeaderElement.appendChild(login_link);
            } else {
                var actionIconsElement = document.createElement("div");
                actionIconsElement.className = "icons";
                myProgramsHeaderElement.appendChild(actionIconsElement);

                var newProjectButton = document.createElement("div");
                newProjectButton.className = "new-project icon";
                newProjectButton.onclick = function (e) {
                    newProjectDialog.open(projectProvider.addNewProject, "Untitled");
                    e.stopPropagation();
                };
                actionIconsElement.appendChild(newProjectButton);
            }

            $(projects).each(function (ind, project) {
                addProject(myProgramsContentElement, project, "My programs");
            });
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
