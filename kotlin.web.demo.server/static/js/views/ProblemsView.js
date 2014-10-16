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


var ProblemsView = function (element, /*Nullable*/ tabs) {
    var scroll = document.createElement("div");
    scroll.className = "scroll";
    element.append(scroll);


    var console = document.createElement("div");
    console.className = "result-view";
    scroll.appendChild(console);

    var instance = {
        addMessages: function () {
            addMessagesToProblemsView();
        },
        onProjectChange: function (newProject) {
            onProjectChange(newProject);
        },
        clear: function () {
            console.innerHTML = "";
        }
    };

    function onProjectChange() {
        console.innerHTML = "";

        var newProjectData = accordion.getSelectedProject().getProjectData();
        for (var i = 0; i < newProjectData.files.length; i++) {
            var file = newProjectData.files[i];

            var fileProblemsDiv = document.createElement("div");
            fileProblemsDiv.id = file.name.replace(/ /g, "_") + "_problems";
            fileProblemsDiv.style.display = "none";

            var fileProblemsHeader = document.createElement("p");
            fileProblemsHeader.className = "problemsView-expanded-filename-header";
            var arrow = document.createElement("div");
            arrow.className = "arrow";
            fileProblemsHeader.appendChild(arrow);

            var img = document.createElement("div");
            img.className = "kotlin-file-icon";
            fileProblemsHeader.appendChild(img);

            var fileName = document.createElement("span");
            fileName.innerHTML = file.name;
            fileProblemsHeader.appendChild(fileName);
            $(fileProblemsHeader).click(function () {
                if ($(this).next().is(':hidden')) {
                    $(this).next().show();
                    this.className = "problemsView-expanded-filename-header";
                } else {
                    $(this).next().hide();
                    this.className = "problemsView-filename-header";
                }
            });
            fileProblemsDiv.appendChild(fileProblemsHeader);

            var fileProblemsContent = document.createElement("div");
            fileProblemsContent.id = file.name.replace(/ /g, "_") + "_problems_content";
            fileProblemsDiv.appendChild(fileProblemsContent);

            console.appendChild(fileProblemsDiv)
        }
    }

    function addMessagesToProblemsView() {
        if (tabs != null) {
            tabs.tabs("option", "active", 0);
        }

        var projectData = accordion.getSelectedProject().getProjectData();
        for (var i = 0; i < projectData.files.length; i++) {
            var file = projectData.files[i];
            if (file.errors.length > 0) {
                document.getElementById(file.name.replace(/ /g, "_") + "_problems").style.display = "block";
                var fileErrors = document.getElementById(file.name.replace(/ /g, "_") + "_problems_content");
                fileErrors.innerHTML = "";
                for (var j = 0; j < file.errors.length; j++) {
                    var error = file.errors[j];
                    error.message = unEscapeString(error.message);
                    var problem = createElementForProblemsView(error);
                    fileErrors.appendChild(problem);
                }
            } else {
                document.getElementById(file.name.replace(/ /g, "_") + "_problems").style.display = "none";
            }
        }
    }

    function createElementForProblemsView(problem) {
        var p = document.createElement("p");
        var img = document.createElement("div");
        if (problem.severity == 'WARNING') {
            img.className = "problemsViewWarningImg";
            p.className = "problemsViewWarning";
        } else if (problem.severity == 'STACKTRACE') {
            p.className = "problemsViewStacktrace";
        } else {
            img.className = "problemsViewErrorImg";
            p.className = "problemsViewError";
        }
        p.appendChild(img);


        var typeStr;
        if (problem.severity == "ERROR") {
            typeStr = "Error";
        } else if (problem.severity == "WARNING") {
            typeStr = "Warning";
        } else if (problem.severity == "INFO") {
            typeStr = "Info";
        }
        var titleDiv = document.createElement("span");
        if (problem.interval.start == null) {
            titleDiv.innerHTML = typeStr + ": " + problem.message;
        } else {
            var span = document.createElement("span");
            var pos = problem.interval.start;

            titleDiv.innerHTML = typeStr + ":(" + pos.line + ", " + pos.ch + ") " + problem.message;
        }

        p.appendChild(titleDiv);
        $(p).click((function () {
            function click() {
                editor.setCursor(pos.line, pos.ch);
                editor.focus();
            }

            return click;
        })(problem.interval.start));
        return p;
    }


    return instance;
};