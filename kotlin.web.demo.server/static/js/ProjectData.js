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
 * Created by Semyon.Atamas on 10/13/2014.
 */
var ProjectData = (function () {

    function ProjectData(/*nullable*/ content) {
        var instance = {
            name: "",
            files: [],
            args: "",
            confType: "",
            originUrl: null,
            parent: "",
            getModifiableContent: function () {
                return {
                    name: instance.name,
                    parent: instance.parent,
                    args: instance.args,
                    confType: instance.confType,
                    originUrl: instance.originUrl,
                    files: instance.files.filter(function (file) {
                        return file.modifiable;
                    })
                };
            },
            processHighlightingResult: function (errors) {
                for (var i = 0; i < instance.files.length; i++) {
                    instance.files[i].errors = errors[instance.files[i].name];
                }
            },
            hasErrors: function () {
                for (var i = 0; i < instance.files.length; i++) {
                    var errors = instance.files[i].errors;
                    for (var j = 0; j < errors.length; j++) {
                        if (errors[j].severity == "ERROR") {
                            return true;
                        }
                    }
                }
                return false;
            }
        };

        if (content != null) {
            var originUrl = content.originUrl;
            instance.name = content.name;
            instance.confType = content.confTrpe;
            instance.parent = content.parent;
        }
        return instance;
    }

    return ProjectData;
})();