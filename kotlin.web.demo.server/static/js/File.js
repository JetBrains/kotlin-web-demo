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

/**
 * @constructor
 */
var File = (function () {

    function File(project, content) {
        var instance = {
            toJSON: function () {
                return {
                    name: name,
                    text: text,
                    publicId: publicId
                }
            },
            save: function () {
                if (project.getType() == ProjectType.USER_PROJECT) {
                    fileProvider.saveFile(publicId, instance, function () {
                        originalText = text;
                        instance.compareContent();
                    });
                } else {
                    project.saveToLocalStorage();
                }
            },
            loadOriginal: function () {
                fileProvider.loadOriginalFile(instance, setFileData);
            },
            onFileSaved: function () {
            },
            compareContent: function () {
                if (text == originalText) {
                    instance.onUnmodified();
                } else {
                    instance.onModified();
                }
            },
            onModified: function () {
            },
            onUnmodified: function () {
            },
            getName: function () {
                return name;
            },
            setName: function (newName) {
                name = newName;
            },
            isModifiable: function () {
                return modifiable;
            },
            getErrors: function () {
                return errors;
            },
            setErrors: function (newErrors) {
                errors = newErrors;
            },
            getText: function () {
                return text;
            },
            getPublicId: function () {
                return publicId;
            },
            setText: function (newText) {
                text = newText;
                instance.compareContent();
            },
            setChangesHistory: function (history) {
                changesHistory = history;
            },
            getChangesHistory: function () {
                return changesHistory;
            },
            getProject: function () {
                return project;
            }
        };

        var name = "";
        var originalText = "";
        var text = "";
        var modifiable = true;
        var errors = [];
        var type = "Kotlin";
        var publicId = "";
        var changesHistory = null;

        File.defaultFileContent = (function () {
            return {
                name: "",
                content: "",
                modifiable: true,
                publicId: ""
            };
        })();

        function setFileData(data) {
            if (data != null) {
                name = data.name;
                text = originalText = data.content;
                modifiable = data.modifiable;
                publicId = data.publicId;
                changesHistory = null;
            }
        }

        setFileData(content);


        return instance;
    }

    return File;
})();