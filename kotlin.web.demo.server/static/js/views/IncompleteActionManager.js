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
            registerAction: function (id, timePoint, callback) {
                if (!(id in actions)) {
                    actions[id] = ({timePoint: timePoint, callback: callback})
                } else {
                    throw "You can't register actions with same id."
                }
            },
            incomplete: function (id) {
                if (id in actions) {
                    incompletedActions.push(id);
                } else {
                    throw "Action not registered";
                }
            },
            checkTimepoint: function (timePoint) {
                for (var id in incompletedActions) {
                    if (actions[id].timePoint == timePoint) {
                        actions[id].callback();
                    }
                }
            }
        };

        var actions = [];
        var incompletedActions = [];
        return instance;
    }

    return IncompleteActionManager;
})();