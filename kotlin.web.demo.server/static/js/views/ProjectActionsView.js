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
    function ProjectActionsView(element) {
        var instance = {
            registerStatus: function (id, message, actions) {
                if (id in statuses) {
                    throw "Status " + id + " already exists";
                } else {
                    statuses[id] = {message: message, actions: actions}
                }
            },
            setStatus: function (statusId) {
                setStatus(statusId);
            }
        };

        var statuses = {
            default: {}
        };

        function setStatus(statusId) {
            if (statusId in statuses) {
                if (statusId == "default") {
                    element.style.display = "none";
                    editor.resize();
                    return;
                }

                var status = statuses[statusId];
                element.innerHTML = "";
                element.style.display = "block";

                var message = document.createElement("span");
                message.className = "editor-notifications-message";
                message.innerHTML = status.message;
                element.appendChild(message);

                for (var i = 0; i < status.actions.length; ++i) {
                    var action = document.createElement("div");
                    action.className = "editor-notifications-action";
                    action.innerHTML = status.actions[i].name;
                    action.onclick = status.actions[i].callback;
                    element.appendChild(action);
                }
                editor.resize();
            } else {
                throw(statusId + " is not a status");
            }
        }

        return instance
    }


    return ProjectActionsView;
})();