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
        var publicLinks = JSON.parse(localStorage.getItem("publicLinks"));
        if (publicLinks == null) publicLinks = [];
        var selectedProject = null;
        var instance = {
            onLoadExampleHeaders: function (data) {
                if (Object.prototype.toString.call(data) === '[object Array]') {
                    for (var i = 0; i < data.length; i++) {
                        addFolder(data[i]);
                    }
                }
                addMyProjectsFolder();

                element.accordion("refresh");
                element.accordion("option", "active", 0);
                if (!loginView.isLoggedIn()) {
                    addPublicLinkFolder();
                    loadFirstItem();
                }
            },

            onLoadUserProjectsHeaders: function (data) {
                for (var i = 0; i < data.length; i++) {
                    addProject("My Programs", document.getElementById("My_Programs_content"), data[i].name, data[i].publicId);
                }
                addNewProjectButton();
                addPublicLinkFolder();
                loadFirstItem();
            },
            saveProject: function () {
                selectedProject.save();
            },
            saveProjectAs: function () {
                selectedProject.saveAs();
            },
            loadAllContent: function () {
                element.html("");
                headersProvider.getAllExamples();
            },
            addNewProject: function (name, publicId, fileId, /*nullable*/ content) {
                addNewProject(name, publicId, fileId, content);
            },
            onLoadCode: function (example, isProgram) {
            },
            onSaveProgram: function () {
            },
            deleteProject: function (publicId) {
                deleteProject(publicId);
            },
            onFail: function (exception, messageForStatusBar) {
            },
            onLogout: function () {
                localStorage.setItem("openedItemUrl", accordion.getSelectedProject().getUrl());
                downloadedProjects = {};
                selectedProject = null;
                instance.loadAllContent();
            },
            getSelectedProject: function () {
                return selectedProject;
            },
            verifyNewProjectname: function (projectName) {
                for (var url in downloadedProjects) {
                    if (url == "My_Programs&name=" + projectName.replace(/ /g, "_")) {
                        return false;
                    }
                }
                return true;
            },
            onBeforeUnload: function () {
                localStorage.setItem("publicLinks", JSON.stringify(publicLinks))
            }
        };

        var headersProvider = (function () {
            var provider = new AccordionHeadersProvider(instance.onLoadExampleHeaders, instance.onLoadUserProjectsHeaders);

            provider.onFail = function (data, status) {
                console.log(data);
                statusBarView.setStatus(status);
                statusBarView.setStatus(status)
            };

            provider.onRenameProject = function (publicId, newName) {
                renameProject(publicId, newName);
            };

            provider.onProjectInfoLoaded = function (data) {
                if (data.isUserProject) {
                    onProjectHeaderClick(createUserProjectUrl(data.projectId));
                } else {
                    for (var i = 0; i < publicLinks.length; ++i) {
                        if (publicLinks[i].id == data.projectId) {
                            onProjectHeaderClick(createUserProjectUrl(data.projectId));
                            return
                        }
                    }

                    publicLinks.push({name: data.name, id: data.projectId});
                    addNewPublicLink(data.name, data.projectId);
                    onProjectHeaderClick(createUserProjectUrl(data.projectId));
                }
            };

            return provider;
        })();


        var newProjectDialog = new InputDialogView("Add new project", "Project name:", "Add");
        newProjectDialog.verify = instance.verifyNewProjectname;
        var renameProjectDialog = new InputDialogView("Rename project", "Project name:", "Rename");
        renameProjectDialog.verify = instance.verifyNewProjectname;

        function createProject(name, contentElement, content, projectId, fileId) {
            var url = createUserProjectUrl(projectId);
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
                }
            }
            downloadedProjects[url] = new Project(url, contentElement, content, ProjectType.USER_PROJECT);
        }

        function loadFirstItem() {

            var folder = getParameterByName("folder");
            var project = getParameterByName("project");
            var file = getParameterByName("file");
            var id = getParameterByName("id");

            if (folder != "" && project != "" && file != "") {
                onProjectHeaderClick(createExampleUrl(project, folder));
            } else if (id != "") {
                headersProvider.getInfoAboutProject(id);
            } else {
                var openedItemUrl = localStorage.getItem("openedItemUrl");
                localStorage.removeItem("openedItemUrl");
                if (openedItemUrl != null && document.getElementById(openedItemUrl) != null) {
                    document.getElementById(openedItemUrl).parentNode.parentNode.previousSibling.click();
                    onProjectHeaderClick(openedItemUrl);
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
            var cont = document.getElementById("My_Programs_content");

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

            addNewProjectDiv.onclick = newProjectDialog.open.bind(null, headersProvider.addNewProject, "Project");
            cont.appendChild(addNewProjectDiv);
        }


        function createProjectHeader(folder, name, /*nullable*/ id) {
            if (id == null) {
                downloadedProjects[createExampleUrl(name, folder)] = null;
            } else {
                downloadedProjects[createUserProjectUrl(id)] = null;
            }
            var projectHeader = document.createElement("h4");
            projectHeader.className = "examples-project-name";
            var img = document.createElement("div");
            img.className = "arrow";
            projectHeader.appendChild(img);
            if (id == null) {
                projectHeader.id = createExampleUrl(name, folder) + "_header";
            } else {
                projectHeader.id = "project_" + id + "_header";
            }
            projectHeader.onclick = function (event) {
                onProjectHeaderClick(this.id.substring(0, this.id.indexOf("_header")));
            };


            var nameSpan = document.createElement("span");
            nameSpan.className = "file-name-span";
            nameSpan.style.cursor = "pointer";
            nameSpan.innerHTML = name;
            projectHeader.appendChild(nameSpan);

            if (folder == "My Programs") {
                var deleteButton = document.createElement("div");
                deleteButton.className = "delete-img";
                deleteButton.title = "Delete this project";
                deleteButton.onclick = function (event) {
                    var publicId = this.parentNode.id.substring("project_".length, this.parentNode.id.indexOf("_header"));
                    var name = this.parentNode.childNodes[1].innerHTML;
                    if (confirm("Delete project " + name + "?")) {
                        headersProvider.deleteProject(publicId);
                    }
                    event.stopPropagation();
                };


                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename this file";
                renameImg.onclick = function (event) {
                    var publicId = this.parentNode.id.substring("project_".length, this.parentNode.id.indexOf("_header"));
                    var name = this.parentNode.childNodes[1].innerHTML;
                    renameProjectDialog.open(headersProvider.renameProject.bind(null, publicId), name);
                    event.stopPropagation();

                };

                projectHeader.appendChild(deleteButton);
                projectHeader.appendChild(renameImg);
            }

            return projectHeader;
        }

        function addNewProject(name, publicId, fileId, /*nullable*/ content) {
            var myProg = document.getElementById("My_Programs_content");
            var newProjectNode = myProg.lastChild;
            myProg.insertBefore(createProjectHeader("My Programs", name, publicId), newProjectNode);


            var exampleContent = document.createElement("div");
            exampleContent.id = createExampleUrl(name, "My Programs") + "_content";
            myProg.insertBefore(exampleContent, newProjectNode);


            createProject(name, exampleContent, content, publicId, fileId);
            element.accordion('option', 'active', element.find("h3").length - 1);
            document.getElementById(createUserProjectUrl(publicId) + "_header").click();
        }

        function addNewPublicLink(name, id) {
            var publicLinks = document.getElementById("Public_Links_content");
            publicLinks.insertBefore(createProjectHeader("Public Links", name, id), publicLinks.firstChild);

            var exampleContent = document.createElement("div");
            exampleContent.id = createUserProjectUrl(id) + "_content";
            publicLinks.insertBefore(exampleContent, publicLinks.firstChild.nextSibling);
        }

        function addProject(folder, element, name, /*nullable*/ id) {
            element.appendChild(createProjectHeader(folder, name, id));

            var exampleContent = document.createElement("div");
            if (id == null) {
                exampleContent.id = createExampleUrl(name, folder) + "_content";
            } else {
                exampleContent.id = createUserProjectUrl(id) + "_content";
            }
            element.appendChild(exampleContent);
        }

        function deleteProject(publicId) {
            var header = document.getElementById(createUserProjectUrl(publicId) + "_header");
            header.parentNode.removeChild(header);

            var project = downloadedProjects[createUserProjectUrl(publicId)];
            if (project != null) {
                project.onDelete();
                if (project == selectedProject) {
                    selectedProject = null;
                    var myPrograms = document.getElementById("My_Programs_content");
                    if (myPrograms.firstElementChild.id == "add_new_project") {
                        loadFirstItem();
                    } else {
                        myPrograms.firstChild.click();
                    }
                }
            }
            delete downloadedProjects[createUserProjectUrl(publicId)];
        }

        function addPublicLinkFolder() {
            var folder = document.createElement("h3");
            folder.className = "examples-folder-name";
            element.append(folder);

            var folderDiv = document.createElement("div");
            folderDiv.innerHTML = "Public links";
            folderDiv.className = "folder-name-div";
            folder.appendChild(folderDiv);

            var cont = document.createElement("div");
            cont.id = "Public_Links_content";
            element.append(cont);

            if (publicLinks != null) {
                publicLinks.filter(function (publicLink) {
                    for (var url in downloadedProjects) {
                        if (createUserProjectUrl(publicLink.id) == url) {
                            localStorage.removeItem(url);
                            return false;
                        }
                    }
                    addNewPublicLink(publicLink.name, publicLink.id);
                    return true;
                });
            }

            element.accordion("refresh");
        }

        function addFolder(data) {
            var folder = document.createElement("h3");
            var folderDiv = document.createElement("div");
            folder.className = "examples-folder-name";

            folderDiv.innerHTML = data.name;
            folderDiv.className = "folder-name-div";

            folder.appendChild(folderDiv);
            element.append(folder);
            var cont = document.createElement("div");
            var i = 0;
            while (data.examplesOrder[i] != undefined) {
                addProject(data.name, cont, data.examplesOrder[i]);
                i++;
            }

            element.append(cont);
        }

        function addMyProjectsFolder() {
            var myProg = document.createElement("h3");
            myProg.className = "examples-folder-name";
            myProg.innerHTML = "My programs";


            if (!loginView.isLoggedIn()) {
                myProg.style.color = "rgba(0,0,0,0.5)";
                var login_link = document.createElement("span");
                login_link.id = "login-link";
                login_link.className = "login-link";
                login_link.innerHTML = "(please log in)";
                element.find("#login_link").click(function (event) {
                    $("#login-dialog").dialog("open");
                    event.stopPropagation()
                });
                myProg.appendChild(login_link);
            }


            element.append(myProg);
            element.find(".login-link").click(function (event) {
                $("#login-dialog").dialog("open");
                event.stopPropagation()
            });
            var myProgCont = document.createElement("div");
            myProgCont.id = "My_Programs_content";
            element.append(myProgCont);
            if (loginView.isLoggedIn()) {
                headersProvider.getAllPrograms();
            }
        }

        function renameProject(publicId, newName) {
            var element = document.getElementById(createUserProjectUrl(publicId) + "_header").childNodes[1];
            element.innerHTML = newName;
            downloadedProjects[createUserProjectUrl(publicId)].rename(newName);
        }

        function onProjectHeaderClick(url) {
            document.getElementById(url + "_header").parentNode.previousSibling.click();
            var element;
            if (downloadedProjects[url] == null) {
                if (selectedProject != null) {
                    selectedProject.deselect();
                    selectedProject.save();
                    element = document.getElementById(selectedProject.getUrl() + "_header");
                    element.className = "examples-project-name";
                    $(element).next().slideUp();
                }
                var content = document.getElementById(url + "_content");
                var type;
                if (document.getElementById(url + "_header").parentNode.id.startsWith("My_Programs")) {
                    type = ProjectType.USER_PROJECT
                } else if (document.getElementById(url + "_header").parentNode.id.startsWith("Public_Links")) {
                    type = ProjectType.PUBLIC_LINK
                } else {
                    type = ProjectType.EXAMPLE
                }
                downloadedProjects[url] = new Project(url, content, null, type);
                selectedProject = downloadedProjects[url];
            } else if (downloadedProjects[url] != selectedProject) {
                if (selectedProject != null) {
                    selectedProject.deselect();
                    selectedProject.save();
                    element = document.getElementById(selectedProject.getUrl() + "_header");
                    element.className = "examples-project-name";
                    $(element).next().slideUp();
                }
                selectedProject = downloadedProjects[url];
                element = document.getElementById(selectedProject.getUrl() + "_header");
                element.className = "expanded-project-name";
                $(element).next().slideDown();
                selectedProject.select();
            } else {
                element = document.getElementById(selectedProject.getUrl() + "_header");
                if (element.className.indexOf("expanded-project-name") > -1) {
                    element.className = "current-project-name";
                    $(element).next().slideUp()
                } else {
                    element.className = "expanded-project-name";
                    $(element).next().slideDown()
                }
            }

//            $("span[id='" + ExamplesView.getLastSelectedItem() + "']").parent().attr("class", "selected-example");
        }

        return instance;
    }


    return AccordionView;
})();
