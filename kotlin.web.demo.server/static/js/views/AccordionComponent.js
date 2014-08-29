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
                loadFirstItem();
            },
            onLoadUserProjectsHeaders: function (data) {
                for (var i = 0; i < data.length; i++) {
                    addProject("My Programs", document.getElementById("My_Programs_content"), data[i]);
                }
                addNewProjectButton();
            },
            saveProgram: function () {
                programsView.saveProgram();
            },
            loadAllContent: function () {
                element.html("");
                headersProvider.getAllExamples();
            },
            addNewProject: function (name) {
                createProject(name);
                this.loadAllContent();
            },
            onLoadCode: function (example, isProgram) {
            },
            onSaveProgram: function () {
            },
            deleteProject: function () {
                this.loadAllContent();
            },
            onFail: function (exception, messageForStatusBar) {
            },
            onLogout: function(){
                downloadedProjects = {};
                selectedProject = null;
                instance.loadAllContent();
            },
            getSelectedProject: function () {
                return selectedProject;
            }
        };

        var headersProvider = (function () {
            return new AccordionHeadersProvider(instance.onLoadExampleHeaders, instance.onLoadUserProjectsHeaders, instance.onFail);
        })();

        var newProjectDialog = new InputDialogView("Add new project", "Project name:", "Add", headersProvider.addNewProject);

        function createProject(name) {
            var url = getProjectURL("My Programs", name);
            downloadedProjects[url] = new Project(url, document.getElementById(url + "_content"), {
                name: name,
                parent: "My Program",
                args: "",
                confType: "java",
                help: "",
                files: []
            });
        }

        function loadFirstItem() {
            var urlAct = [location.protocol, '//', location.host, "/"].join('');
            var url = document.location.href;
            if (url.indexOf(urlAct) != -1) {
                url = url.substring(url.indexOf(urlAct) + urlAct.length);
                var exampleStr = "?folder=";
                var publicLink = "?publicLink=";
                if (url.indexOf(exampleStr) == 0) {
                    url = url.substring(exampleStr.length);
                    $("#" + getFolderNameByUrl(url)).click();
                    examplesModel.loadExample(url);
                } else if (url.indexOf(publicLink) == 0) {
                    programsView.loadProgram(createExampleUrl(url.substring(publicLink.length), "My Programs"));
                } else {
                    document.getElementById("Hello,_world!&name=Simplest_version").click();
                }
            }
        }


        function addNewProjectButton() {
            var cont = document.getElementById("My_Programs_content");

            var addNewProjectDiv = document.createElement("div");

            var addNewProjectIcon = document.createElement("div");
            addNewProjectIcon.innerHTML = "+";
            addNewProjectDiv.appendChild(addNewProjectIcon);

            var addNewProjectText = document.createElement("div");
            addNewProjectText.innerHTML = "Add new project";
            addNewProjectText.className = "examples-project-name";
            addNewProjectDiv.appendChild(addNewProjectText);

            addNewProjectDiv.onclick = newProjectDialog.open;
            cont.appendChild(addNewProjectDiv);
        }


        function addProject(folder, element, name) {
            var file = document.createElement("h4");
            file.className = "examples-project-name";
            var img = document.createElement("div");
            img.className = "arrow";
            file.appendChild(img);
            file.id = createExampleUrl(name, folder) + "_header";


            var nameSpan = document.createElement("span");
            nameSpan.id = createExampleUrl(name, folder);
            nameSpan.className = "file-name-span";
            nameSpan.style.cursor = "pointer";
            nameSpan.onclick = function () {
                onProjectHeaderClick(this.id);
            };
            nameSpan.innerHTML = name;
            file.appendChild(nameSpan);

            if(folder == "My Programs"){
                var deleteButton = document.createElement("div");
                deleteButton.className = "delete-img";
                deleteButton.title = "Delete this project";
                deleteButton.onclick = (function(url){
                    return function() {
                        headersProvider.deleteProject(url);
                    }
                })(createExampleUrl(name, folder));
                file.appendChild(deleteButton);
            }

            element.appendChild(file);

            var exampleContent = document.createElement("div");
            exampleContent.id = createExampleUrl(name, folder) + "_content";
            element.appendChild(exampleContent);


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
                $("#save").attr("title", "Save your program(you must be logged in)");
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

        function onProjectHeaderClick(url) {
            var element;
            if (downloadedProjects[url] == undefined) {
                if (selectedProject != null) {
                    selectedProject.save();
                    element = document.getElementById(selectedProject.getURL() + "_header");
                    element.className = "examples-project-name";
                    $(element).next().slideUp();
                }
                var content = document.getElementById(url + "_content");
                downloadedProjects[url] = new Project(url, content);
                selectedProject = downloadedProjects[url];
            } else if (downloadedProjects[url] != selectedProject) {
                if (selectedProject != null) {
                    selectedProject.save();
                    element = document.getElementById(selectedProject.getURL() + "_header");
                    element.className = "examples-project-name";
                    $(element).next().slideUp();
                }
                selectedProject = downloadedProjects[url];
                element = document.getElementById(selectedProject.getURL() + "_header");
                element.className = "expanded-project-name";
                $(element).next().slideDown();
                selectedProject.select();
            } else {
                element = document.getElementById(selectedProject.getURL() + "_header");
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

        function getProjectURL(parent, name) {
            return replaceAll(parent, " ", "_") + "&name=" + replaceAll(name, " ", "_");
        }

        return instance;
    }


    return AccordionView;
})();
