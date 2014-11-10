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

var FileData = (function () {

    function FileData(project, /*nullable*/ data) {
        var instance = {
            name: "",
            originalContent: "",
            content: "",
            modifiable: true,
            errors: [],
            type: "Kotlin",
            publicId: "",
            setContent: function (content) {
                instance.content = content;
                instance.compareContent();
            },
            save: function () {
                if (project.getType() == ProjectType.USER_PROJECT) {
                    fileProvider.saveFile(instance.publicId, instance, function () {
                        instance.originalContent = instance.content;
                        instance.compareContent();
                    });
                } else {
                    project.saveToLocalStorage();
                }
            },
            setChangesHistory: function (history) {
                changesHistory = history;
            },
            getChangesHistory: function () {
                return changesHistory;
            },
            getProject: function () {
                return project;
            },
            loadOriginal: function () {
                fileProvider.loadOriginalFile(instance, setFileData);
            },
            onFileSaved: function () {
            },
            compareContent: function () {
                if (instance.content == instance.originalContent) {
                    instance.onUnmodified();
                } else {
                    instance.onModified();
                }
            },
            onModified: function () {
            },
            onUnmodified: function () {
            }
        };

        var changesHistory = null;

        function setFileData(data) {
            if (data != null) {
                instance.name = data.name;
                instance.content = instance.originalContent = data.content;
                instance.modifiable = data.modifiable;
                instance.publicId = data.publicId;
                changesHistory = null;
            }
        }

        setFileData(data);


        return instance;
    }

    return FileData;
})
();