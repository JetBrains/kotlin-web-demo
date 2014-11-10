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

    function ProjectData(type, publicId, /*nullable*/ content) {
        var instance = {
            name: "",
            files: [],
            args: "",
            confType: "java",
            originUrl: null,
            parent: "",
            help: "",
            getModifiableContent: function () {
                return {
                    name: instance.name,
                    parent: instance.parent,
                    args: instance.args,
                    confType: instance.confType,
                    originUrl: instance.originUrl,
                    files: instance.files.filter(function (file) {
                        return file.isModifiable();
                    })
                };
            },
            processHighlightingResult: function (errors) {
                for (var i = 0; i < instance.files.length; i++) {
                    instance.files[i].setErrors(errors[instance.files[i].getName()]);
                }
            },
            hasErrors: function () {
                for (var i = 0; i < instance.files.length; i++) {
                    var errors = instance.files[i].getErrors();
                    for (var j = 0; j < errors.length; j++) {
                        if (errors[j].severity == "ERROR") {
                            return true;
                        }
                    }
                }
                return false;
            },
            getType: function () {
                return type
            },
            setArguments: function (args) {
                instance.args = args;
                instance.onChange();
            },
            setConfiguration: function (confType) {
                instance.confType = confType;
                instance.onChange();
            },
            saveToLocalStorage: function () {
                if (type == ProjectType.USER_PROJECT) throw "User project shouldn't be saved in local storage";
                localStorage.setItem(publicId, JSON.stringify(instance));
            },
            onChange: function () {

            }
        };

        if (content != null) {
            var originUrl = content.originUrl;
            instance.name = content.name;
            instance.confType = content.confType;
            instance.parent = content.parent;
            instance.help = content.help;
        }
        return instance;
    }

    return ProjectData;
})();