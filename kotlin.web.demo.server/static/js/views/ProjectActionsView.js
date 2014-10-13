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
 * Created by Semyon.Atamas on 9/3/2014.
 */


var ProjectActionsView = (function () {
    function ProjectActionsView(element, project) {

        var status = "default";
        var instance = {
            setStatus: function (newStatus) {
                status = newStatus;
                instance.refresh();
            },
            refresh: function () {
                switch (status) {
                    case "unsavedChanges":
                        setUnsavedChangesStatus();
                        break;
                    case "default":
                        element.innerHTML = "";
                        element.style.display = "none";
                        break;
                    default:
                        throw "Unknown project actions view status";
                }
                editor.resize();
            },
            hide: function () {
                element.style.display = "none";
            },
            show: function () {
                element.style.display = "block";
            }
        };

        function setUnsavedChangesStatus() {
            if (project.getType() == ProjectType.EXAMPLE || project.getType() == ProjectType.PUBLIC_LINK) {
                element.innerHTML = "";
                element.style.display = "block";

                var message = document.createElement("span");
                message.className = "editor-notifications-messages";
                if (project.getType() == ProjectType.EXAMPLE) {
                    message.innerHTML = "This is local version of example";
                } else {
                    message.innerHTML = "This is local version of project";
                }
                element.appendChild(message);

                if (project.getType() == ProjectType.EXAMPLE) {
                    var restore = document.createElement("div");
                    restore.className = "editor-notifications-action";
                    restore.innerHTML = "Load original";
                    restore.id = "load-original-action";
                    restore.onclick = project.restoreDefault;
                    element.appendChild(restore);
                } else {
                    project.checkIfDatabaseCopyExists(function () {
                        var restore = document.createElement("div");
                        restore.className = "editor-notifications-action";
                        restore.innerHTML = "Load original";
                        restore.id = "load-original-action";
                        restore.onclick = project.checkIfDatabaseCopyExists.bind(null, project.restoreDefault, function () {
                            restore.parentNode.removeChild(restore)
                        });
                        element.appendChild(restore);
                    }, function () {
                    })
                }


                var save = document.createElement("div");
                save.className = "editor-notifications-action";
                save.innerHTML = "Save";
                save.onclick = project.saveAs;
                element.appendChild(save);
            } else {
                element.innerHTML = "";
                element.style.display = "none";
            }
        }

        return instance
    }


    return ProjectActionsView;
})();