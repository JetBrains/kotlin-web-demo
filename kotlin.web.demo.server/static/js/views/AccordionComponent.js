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
        var lastSelectedItem = 0;

        element.accordion({
            heightStyle: "content",
            navigation: true,
            active: 0,
            icons: {
                activeHeader: "examples-open-folder-icon",
                header: "examples-closed-folder-icon"
            }
        }).find(".login-link").click(function (event) {
            $("#login-dialog").dialog("open");
            event.stopPropagation()
        });

        var examplesModel = new ExamplesModel();
        var programsModel = new ProgramsModel();
        var programsView = new ProgramsView(programsModel);

        var downloadedProjects = {};
        var selectedProject = null;
        var instance = {
            onLoadProjects: function(data){
                for(var i = 0; i < data.length; i++){
                    var project = new Example();
                    downloadedProjects[project.getURL()] = project;
                }
            },
            onLoadHeaders: function(data){
                if(Object.prototype.toString.call(data) === '[object Array]') {
                    for (var i = 0; i < data.length; i++) {
                        addFolder(data[i]);
                    }
                } else{
                    addFolder(data);
                }
                element.accordion("refresh");
                element.accordion("option", "active", 0);
                loadFirstItem()
            },
            addHeader: function(name){
                console.log("anme");
            },
            saveProgram: function () {
                programsView.saveProgram();
            },
            saveAsProgram: function(){
                programsView.saveAsProgram();
            },
            loadAllContent: function () {
                element.html("");
                examplesModel.getAllExamples();
                if(loginView.isLoggedIn()) {
                    programsModel.getAllPrograms();
                }
            },
            setConfiguration: programsView.setConfiguration,
            onLoadCode: function (example, isProgram) {
            },
            onSaveProgram: function () {
            },
            onDeleteProgram: function () {
            },
            onLoadAllContent: function () {
            },
            onFail: function (exception, messageForStatusBar) {
            },
            getSelectedProject: function(){
                return selectedProject;
            }

        };

//        examplesView.onAllExamplesLoaded = function () {
//            programsView.loadAllContent()
//        };
        programsView.onAllProgramsLoaded = function () {
            makeAccordion();
            loadFirstItem();
            instance.onLoadAllContent();
        };


        ProgramsView.setLastSelectedItem = function (item) {
            lastSelectedItem = item;
        };
        ProgramsView.getLastSelectedItem = function () {
            return lastSelectedItem;
        };
        ProgramsView.getMainElement = function () {
            return element;
        };


        programsModel.onFail = function (data, statusBarMessage) {
            instance.onFail(data, statusBarMessage);
        };
        programsModel.onLoadProgram = function (program) {
            instance.onLoadCode(program, true);
        };
        programsModel.onGeneratePublicLink = function (data) {
            programsView.generatePublicLink(data);
        };
        programsModel.onDeleteProgram = function (data) {
            programsView.deleteProgram(data);
            instance.onDeleteProgram();
        };
        programsModel.onSaveProgram = function (data) {
            programsView.saveProgramWithName(data);
            instance.onSaveProgram();
        };
        programsModel.onAllProgramsLoaded = function (data) {
            instance.onLoadHeaders(data);
        };

        examplesModel.onLoadExample = function (example) {
            onLoadExample(example);
            instance.onLoadCode(example, false);
        };
        examplesModel.onAllExamplesLoaded = instance.onLoadHeaders;

        function makeAccordion() {
            $("#examples-list").accordion({
                heightStyle: "content",
                navigation: true,
                icons: {
                    activeHeader: "examples-open-folder-icon",
                    header: "examples-closed-folder-icon"
                }
            }).find(".login-link").click(function (event) {
                    $("#login-dialog").dialog("open");
                    event.stopPropagation()
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
                    examplesModel.loadExample("Hello,_world!&name=Simplest_version");
                }
            }
        }

        function addProject(folder, cont, name) {
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
                if (downloadedProjects[this.id] == undefined) {
                    examplesModel.loadExample(this.id);
                } else {
                    showProject(this.id)
                }
            };
            nameSpan.innerHTML = name;

            file.appendChild(nameSpan);

            cont.appendChild(file);

            var exampleContent = document.createElement("div");
            exampleContent.id = createExampleUrl(name, folder) + "_content";
            cont.appendChild(exampleContent);
        }

        function addFolder(data){
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

            if(data.name == "My Programs"){
                var addNewProjectDiv = document.createElement("div");

                var addNewProjectIcon = document.createElement("div");
                addNewProjectIcon.innerHTML = "+";
                addNewProjectDiv.appendChild(addNewProjectIcon);

                var addNewProjectText = document.createElement("div");
                addNewProjectText.innerHTML = "Add new project";
                addNewProjectText.className ="examples-project-name";
                addNewProjectDiv.appendChild(addNewProjectText);

                addNewProjectDiv.onclick = function() {
                    programsModel.addNewProject("azaza");
                };

                cont.appendChild(addNewProjectDiv);
            }

            element.append(cont);
        }

        function showProject(url) {
            if (downloadedProjects[url] != selectedProject) {
                if(selectedProject != null) {
                    selectedProject.save();
                    var element = document.getElementById(selectedProject.getURL() + "_header");
                    element.className = "examples-project-name";
                    $(element).next().slideUp();
                }
                selectedProject = downloadedProjects[url];
                element = document.getElementById(selectedProject.getURL() + "_header");
                element.className = "expanded-project-name";
                $(element).next().slideDown();
                selectedProject.select();
            } else {
                var element = document.getElementById(selectedProject.getURL() + "_header");
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


        function onLoadExample(exampleContent) {
            var example = new Example(exampleContent);
            downloadedProjects[example.getURL()] = example;
            showProject(example.getURL());
        }

        return instance;
    }


    return AccordionView;
})();
