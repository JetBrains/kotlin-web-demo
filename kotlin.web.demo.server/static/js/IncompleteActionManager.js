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
 * Created by Semyon.Atamas on 11/13/2014.
 */

var IncompleteActionManager = (function () {
    function IncompleteActionManager() {
        var instance = {
            registerAction: function (id, timePoint, onRegistered, callback) {
                if (!(id in actions)) {
                    actions[id] = ({timePoint: timePoint, callback: callback, onRegistered: onRegistered})
                } else {
                    throw "You can't register actions with same id."
                }
            },
            incomplete: function (id) {
                if (id in actions) {
                    incompleteActions.push(id);
                    actions[id].onRegistered();
                } else {
                    throw "Action not registered";
                }
            },
            cancel: function(id){
                var index = incompleteActions.indexOf(id);
                if (index > -1) {
                    incompleteActions.splice(index, 1);
                }
            },
            checkTimepoint: function (timePoint) {
                timePoint = "on" + timePoint.capitalize();
                for (var i = 0; i < incompleteActions.length; ++i) {
                    if (actions[incompleteActions[i]].timePoint == timePoint) {
                        actions[incompleteActions[i]].callback();
                    }
                }
                incompleteActions = incompleteActions.filter(function (element) {
                    return element.timePoint == timePoint;
                })
            },
            onBeforeUnload: function () {
                localStorage.setItem("incompleteActions", JSON.stringify(incompleteActions))
            }
        };

        var actions = {};
        var incompleteActions = localStorage.getItem("incompleteActions") == null ? [] : JSON.parse(localStorage.getItem("incompleteActions"));
        localStorage.removeItem("incompleteActions");
        return instance;
    }

    return IncompleteActionManager;
})();