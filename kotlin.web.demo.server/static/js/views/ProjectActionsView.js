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
                    case "localVersion":
                        setLocalVersionStatus();
                        break;
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
            if (!project.isUserProject()) {
                element.innerHTML = "";
                element.style.display = "block";

                var message = document.createElement("span");
                message.className = "editor-notifications-messages";
                message.innerHTML = "This is local version of example";
                element.appendChild(message);

                var restore = document.createElement("div");
                restore.className = "editor-notifications-action";
                restore.innerHTML = "Load original";
                restore.onclick = project.restoreDefault;
                element.appendChild(restore);

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

        function setLocalVersionStatus() {
            element.innerHTML = "";
            element.style.display = "block";

            var message = document.createElement("span");
            message.className = "editor-notifications-messages";
            message.innerHTML = "Example was loaded from the local storage";
            element.appendChild(message);

            if (!project.isUserProject()) {
                var restore = document.createElement("div");
                restore.className = "editor-notifications-action";
                restore.innerHTML = "Load original";
                restore.onclick = project.restoreDefault;
                element.appendChild(restore);
            }
        }

        return instance
    }


    return ProjectActionsView;
})();