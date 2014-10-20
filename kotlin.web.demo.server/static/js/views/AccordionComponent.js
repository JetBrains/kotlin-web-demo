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
        var selectedProject = null;
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
                        $(element).accordion("refresh");
                        loadFirstItem();
                    }
                )
                ;
            },
            addNewProject: function (name, publicId, fileId, /*Nullable*/ content) {
                addProject(myProgramsContentElement, {name: name, publicId: publicId, type: ProjectType.USER_PROJECT});

                if (content == null) {
                    var filename = name.endsWith(".kt") ? name : name + ".kt";
                    content = {
                        name: name,
                        parent: "My Program",
                        args: "",
                        confType: "java",
                        help: "",
                        files: [
                            {name: filename, content: "", modifiable: true, publicId: fileId}
                        ]
                    };
                }

                projects[publicId].createProject(content);
                selectProject(publicId);
            },
            onLogout: function () {
                localStorage.setItem("openedItemId", instance.getSelectedProject().getPublicId());
                projects = {};
                selectedProject = null;
                instance.loadAllContent();
            },
            getSelectedProject: function () {
                return selectedProject;
            },
            verifyNewProjectname: function (projectName) {
                for (var url in projects) {
                    if (projects[url].getName() == projectName &&
                        projects[url].getType() == ProjectType.USER_PROJECT) {
                        return false;
                    }
                }
                return true;
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

            }
        };

        var myProgramsContentElement;
        var publicLinksContentElement;
        var newProjectDialog = new InputDialogView("Add new project", "Project name:", "Add");
        newProjectDialog.verify = instance.verifyNewProjectname;

        function loadFirstItem() {

            var folder = getParameterByName("folder");
            var project = getParameterByName("project");
            var file = getParameterByName("file");
            var id = getParameterByName("id");

            if (folder != "" && project != "" && file != "") {
                selectProject(createExampleId(project, folder));
            } else if (id != "") {
                headersProvider.getHeaderByFilePublicId(id, function (header) {
                    if (header.type == ProjectType.PUBLIC_LINK) {
                        header.timeStamp = new Date().getTime();
                        addProject(publicLinksContentElement, header);
                    } else {
                        addProject(myProgramsContentElement, header);
                    }
                    selectProject(header.publicId);
                });
            } else {
                var openedItemId = localStorage.getItem("openedItemId");
                localStorage.removeItem("openedItemId");
                if (openedItemId != null && document.getElementById(openedItemId) != null) {
                    selectProject(openedItemId);
                    if (localStorage.getItem("incompleteAction") == "save") {
                        localStorage.removeItem("incompleteAction");
                        selectedProject.saveAs();
                    }
                } else {
                    $(element).accordion('option', 'active', 0);
                    element.childNodes[1].firstElementChild.click();
                }
            }
        }


        function addNewProjectButton() {

            var addNewProjectDiv = document.createElement("div");
            addNewProjectDiv.id = "add_new_project";

            var addNewProjectIcon = document.createElement("div");
            addNewProjectIcon.innerHTML = "+";
            addNewProjectDiv.appendChild(addNewProjectIcon);

            var addNewProjectText = document.createElement("div");
            addNewProjectText.innerHTML = "Add new project";
            addNewProjectText.className = "examples-project-name";
            addNewProjectText.style.cursor = "pointer";
            addNewProjectDiv.appendChild(addNewProjectText);

            addNewProjectDiv.onclick = newProjectDialog.open.bind(null, projectProvider.addNewProject, "Project");
            myProgramsContentElement.appendChild(addNewProjectDiv);
        }

        function addProject(/*Element*/ folderContentElement, header) {
            if (header.type == ProjectType.PUBLIC_LINK && projects[header.publicId] != null) {
                return
            } else if (projects[header.publicId] != null) {
                throw("Duplicate project id");
            }
            var projectHeaderElement = document.createElement("div");
            var projectContentElement = document.createElement("div");

            if (folderContentElement == myProgramsContentElement) {
                folderContentElement.insertBefore(projectHeaderElement, folderContentElement.lastElementChild);
                folderContentElement.insertBefore(projectContentElement, folderContentElement.lastElementChild);
            } else if (folderContentElement == publicLinksContentElement) {
                folderContentElement.insertBefore(projectContentElement, folderContentElement.firstElementChild);
                folderContentElement.insertBefore(projectHeaderElement, folderContentElement.firstElementChild);
            } else {
                folderContentElement.appendChild(projectHeaderElement);
                folderContentElement.appendChild(projectContentElement);
            }


            var projectView = new ProjectView(header, projectContentElement, projectHeaderElement);
            projectView.onHeaderClick = selectProject;
            projectView.onDelete = function () {
                if (selectedProject == projects[header.publicId]) {
                    selectedProject = null;
                    history.replaceState({}, "", "index.html");
                    if (myProgramsContentElement.firstElementChild == null || myProgramsContentElement.firstElementChild.id == "add_new_project") {
                        loadFirstItem();
                    } else {
                        myProgramsContentElement.firstChild.click();
                    }
                }
                delete projects[header.publicId];
            };
            projectView.onSelected = instance.onProjectSelected;

            projects[header.publicId] = projectView;
        }

        function addFolder(name) {
            var folder = document.createElement("h3");
            folder.className = "examples-folder-name";
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
                addNewProjectButton();
            }

            return myProgramsContentElement;
        }

        function selectProject(publicId) {
            if (selectedProject != null) {
                selectedProject.deselect();
            }
            selectedProject = projects[publicId];
            selectedProject.select();
        }

        return instance;
    }


    return AccordionView;
})
();
