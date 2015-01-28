/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
        var instance = {
            loadAllContent: function () {
                element.innerHTML = "";
                projects = {};
                headersProvider.getAllHeaders(function (orderedFolderNames, foldersContent) {
                        for (var i = 0; i < orderedFolderNames.length; ++i) {
                            var folderName = orderedFolderNames[i];

                            var folderContentElement;
                            if (folderName == "My programs") {
                                folderContentElement = addMyProjectsFolder();
                            } else if (folderName == "Public links") {
                                folderContentElement = publicLinksContentElement = addFolder("Public links");
                            } else {
                                folderContentElement = addFolder(folderName)
                            }

                            for (var j = 0; j < foldersContent[folderName].length; ++j) {
                                var exampleHeader = foldersContent[folderName][j];
                                addProject(folderContentElement, exampleHeader);
                            }
                        }
                        incompleteActionManager.checkTimepoint("headersLoaded");
                        $(element).accordion("refresh");
                        loadFirstItem();
                    }
                )
                ;
            },
            addNewProject: function (name, publicId, fileId) {
                addProject(myProgramsContentElement, {name: name, publicId: publicId, type: ProjectType.USER_PROJECT});
                projects[publicId].getProjectData().setDefaultContent();
                selectProject(publicId);
                projects[publicId].getProjectData().addEmptyFile(name, publicId);
            },
            addNewProjectWithContent: function (publicId, content) {
                addProject(myProgramsContentElement, {
                    name: content.name,
                    publicId: publicId,
                    type: ProjectType.USER_PROJECT
                });
                projects[publicId].getProjectData().setContent(content);
                selectProject(publicId);
            },
            onLogout: function () {
                localStorage.setItem("openedItemId", instance.getSelectedProject().getPublicId());
                projects = {};
                selectedProjectView = null;
                instance.loadAllContent();
            },
            getSelectedProject: function () {
                return selectedProjectView.getProjectData();
            },
            getSelectedProjectView: function () {
                return selectedProjectView;
            },
            validateNewProjectName: function (projectName) {
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
            onSelectedFileDeleted: function () {

            },
            onUnmodifiedSelectedFile: function () {
            },
            onModifiedSelectedFile: function () {
            },
            onProjectDeleted: function(){

            },
            loadFirstItem: function () {
                loadFirstItem();
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
        var publicLinksContentElement;
        var newProjectDialog = new InputDialogView("Add new project", "Project name:", "Add");
        newProjectDialog.validate = instance.validateNewProjectName;

        function loadFirstItem() {

            var folder = getParameterByName("folder");
            var project = getParameterByName("project");
            var file = getParameterByName("file");
            var project_id = getParameterByName("project_id");

            if (folder != "" && project != "" && file != "") {
                selectProject(createExampleId(project, folder));
            } else if (project_id != "") {
                if (localStorage.getItem(project_id) == null) {
                    var file_id = getParameterByName("id");
                    headersProvider.getHeaderByFilePublicId(file_id, project_id, function (header) {
                        if (!(header.publicId in projects)) {
                            if (header.type == ProjectType.PUBLIC_LINK) {
                                header.timeStamp = new Date().getTime();
                                addProject(publicLinksContentElement, header);
                            } else {
                                throw "Project wasn't downloaded";
                            }
                        }
                        selectProject(header.publicId);
                    });
                } else {
                    selectProject(project_id);
                }
            } else {
                var openedItemId = localStorage.getItem("openedItemId");
                localStorage.removeItem("openedItemId");
                if (openedItemId == null) {
                    $(element).accordion('option', 'active', 0);
                    element.childNodes[1].firstElementChild.click();
                } else {
                    selectProject(openedItemId);
                }
            }
            localStorage.removeItem("openedItemId");
        }

        function addProject(/*Element*/ folderContentElement, header) {
            if (header.type == ProjectType.PUBLIC_LINK && projects[header.publicId] != null) {
                return
            } else if (projects[header.publicId] != null) {
                throw("Duplicate project id");
            }
            var projectHeaderElement = document.createElement("div");
            var projectContentElement = document.createElement("div");

            folderContentElement.appendChild(projectHeaderElement);
            folderContentElement.appendChild(projectContentElement);


            var projectView = new ProjectView(header, projectContentElement, projectHeaderElement);
            projectView.onHeaderClick = selectProject;
            projectView.onDelete = function () {
                if (selectedProjectView == projects[header.publicId]) {
                    history.replaceState("", "", "index.html");
                    selectedProjectView = null;
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

        function addFolder(name) {
            var folder = document.createElement("h3");
            folder.className = "examples-folder-name";
            folder.id = escapeString(name);
            element.appendChild(folder);

            var folderDiv = document.createElement("div");
            folderDiv.innerHTML = name;
            folderDiv.className = "folder-name-div";
            folder.appendChild(folderDiv);

            var cont = document.createElement("div");
            element.appendChild(cont);
            return cont
        }

        function addMyProjectsFolder() {
            var myProg = document.createElement("h3");
            myProg.className = "examples-folder-name";
            myProg.innerHTML = "My programs";
            myProg.id = escapeString("My programs");
            element.appendChild(myProg);

            myProgramsContentElement = document.createElement("div");
            myProgramsContentElement.id = "My_Programs_content";
            element.appendChild(myProgramsContentElement);

            if (!loginView.isLoggedIn()) {
                myProg.style.color = "rgba(0,0,0,0.5)";
                var login_link = document.createElement("span");
                login_link.id = "login-link";
                login_link.className = "login-link";
                login_link.innerHTML = "(please log in)";
                login_link.onclick = function (event) {
                    $("#login-dialog").dialog("open");
                    event.stopPropagation()
                };
                myProg.appendChild(login_link);
            } else {
                var newProjectButton = document.createElement("div");
                newProjectButton.className = "newProjectButton high-res-icon";
                newProjectButton.onclick = function (e) {
                    newProjectDialog.open(projectProvider.addNewProject, "Untitled");
                    e.stopPropagation();
                };
                myProg.appendChild(newProjectButton);
            }

            return myProgramsContentElement;
        }

        function selectProject(publicId) {
            if(selectedProjectView == null || selectedProjectView.getProjectData().getPublicId() != publicId) {
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
