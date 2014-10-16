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
    function AccordionView(element) {
        element.html("");
        element.accordion({
            heightStyle: "content",
            navigation: true,
            active: 0,
            icons: {
                activeHeader: "examples-open-folder-icon",
                header: "examples-closed-folder-icon"
            }
        });
//        var programsView = new ProgramsView(programsModel);

        var downloadedProjects = {};
        var publicLinks = [];
        var selectedProject = null;
        var instance = {
            loadAllContent: function () {
                element.html("");
                headersProvider.getAllExamples();
            },
            onExampleHeadersLoaded: function (data) {
                for (var i = 0; i < data.length; i++) {
                    var folderContent = data[i];

                    var folderContentElement = addFolder(folderContent.name);

                    for (var j = 0; j < folderContent.examplesOrder.length; ++j) {
                        var exampleName = folderContent.examplesOrder[j];
                        addProject(folderContentElement,
                            exampleName, createExampleUrl(exampleName, folderContent.name), ProjectType.EXAMPLE);
                    }
                }


                addMyProjectsFolder();

                if (!loginView.isLoggedIn()) {
                    publicLinksContent = addFolder("Public links");
                    element.accordion("refresh");

                    headersProvider.getAllPublicLinks();
                    loadFirstItem();
                }
            },
            onUserProjectHeadersLoaded: function (data) {
                for (var i = 0; i < data.length; i++) {
                    addProject(myProgramsContentElement, data[i].name, data[i].publicId, ProjectType.USER_PROJECT);
                }
                addNewProjectButton();

                publicLinksContent = addFolder("Public links");
                element.accordion("refresh");

                headersProvider.getAllPublicLinks();
                loadFirstItem();
            },
            onPublicLinksLoaded: function (data) {
                publicLinks = data;
                publicLinks = publicLinks.filter(function (publicLink) {
                    for (var id in downloadedProjects) {
                        if (publicLink.id == id) {
                            localStorage.removeItem(publicLink.id);
                            return false;
                        }
                    }
                    return true;
                });
                for (var i = 0; i < publicLinks.length; ++i) {
                    addProject(publicLinksContent, publicLinks[i].name, publicLinks[i].id, ProjectType.PUBLIC_LINK);
                }
            },
            saveProject: function () {
                selectedProject.save();
            },
            saveProjectAs: function () {
                selectedProject.saveAs();
            },

            addNewProject: function (name, publicId, fileId, /*nullable*/ content) {
                addProject(myProgramsContentElement, name, publicId, ProjectType.USER_PROJECT, true);

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

                downloadedProjects[publicId].createProject(content);
                selectProject(publicId);
            },
            onLogout: function () {
                localStorage.setItem("openedItemId", instance.getSelectedProject().getPublicId());
                downloadedProjects = {};
                selectedProject = null;
                instance.loadAllContent();
            },
            getSelectedProject: function () {
                return selectedProject;
            },
            verifyNewProjectname: function (projectName) {
                for (var url in downloadedProjects) {
                    if (downloadedProjects[url].getName() == projectName &&
                        downloadedProjects[url].getType() == ProjectType.USER_PROJECT) {
                        return false;
                    }
                }
                return true;
            },
            onBeforeUnload: function () {
                localStorage.setItem("publicLinks", JSON.stringify(publicLinks))
            }
        };

        var myProgramsContentElement;
        var publicLinksContent;
        var newProjectDialog = new InputDialogView("Add new project", "Project name:", "Add");
        newProjectDialog.verify = instance.verifyNewProjectname;

        function loadFirstItem() {

            var folder = getParameterByName("folder");
            var project = getParameterByName("project");
            var file = getParameterByName("file");
            var id = getParameterByName("id");

            if (folder != "" && project != "" && file != "") {
                selectProject(createExampleUrl(project, folder));
            } else if (id != "") {
                headersProvider.getInfoAboutProject(id);
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
                    element.accordion('option', 'active', 0);
                    element.children()[1].children[0].click();
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

        function addProject(/*Element*/ element, /*String*/ name, /*String*/ id, type, /*boolean*/ addToEnd) {
            var projectHeader = document.createElement("div");
            var projectContent = document.createElement("div");
            if (addToEnd) {
                element.insertBefore(projectHeader, element.lastChild);
                element.insertBefore(projectContent, element.lastChild);
            } else {
                element.appendChild(projectHeader);
                element.appendChild(projectContent);
            }

            var projectView = new ProjectView(id, name, projectContent, projectHeader, type);
            projectView.onHeaderClick = selectProject;
            projectView.onDelete = function () {
                if (selectedProject == downloadedProjects[id]) {
                    selectedProject = null;
                    var myPrograms = document.getElementById("My_Programs_content");
                    if (myPrograms.firstElementChild.id == "add_new_project") {
                        loadFirstItem();
                    } else {
                        myPrograms.firstChild.click();
                    }
                }
                delete downloadedProjects[id];
            };
            downloadedProjects[id] = projectView
        }

        function addFolder(name) {
            var folder = document.createElement("h3");
            folder.className = "examples-folder-name";
            element.append(folder);

            var folderDiv = document.createElement("div");
            folderDiv.innerHTML = name;
            folderDiv.className = "folder-name-div";
            folder.appendChild(folderDiv);

            var cont = document.createElement("div");
            element.append(cont);
            return cont
        }

        function addMyProjectsFolder() {
            var myProg = document.createElement("h3");
            myProg.className = "examples-folder-name";
            myProg.innerHTML = "My programs";
            element.append(myProg);

            myProgramsContentElement.id = "My_Programs_content";
            element.append(myProgramsContentElement);

            if (!loginView.isLoggedIn()) {
                myProg.style.color = "rgba(0,0,0,0.5)";
                var login_link = document.createElement("span");
                login_link.id = "login-link";
                login_link.className = "login-link";
                login_link.innerHTML = "(please log in)";
                login_link.click = function (event) {
                    $("#login-dialog").dialog("open");
                    event.stopPropagation()
                };
                myProg.appendChild(login_link);
            } else {
                headersProvider.getAllPrograms();
            }
        }

        function selectProject(publicId) {
            if (selectedProject != null) {
                selectedProject.deselect();
            }
            selectedProject = downloadedProjects[publicId];
            selectedProject.select();
        }

        return instance;
    }


    return AccordionView;
})();
