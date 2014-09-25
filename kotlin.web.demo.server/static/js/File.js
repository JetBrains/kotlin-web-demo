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
 * Created by Semyon.Atamas on 9/17/2014.
 */

var File = (function () {
    var renameFileDialog = new InputDialogView("Rename file", "filename", "Rename");

    function File(project, name, content, modifiable, element) {
        var fileNameElement;

        var instance = {
            name: name,
            content: content,
            modifiable: modifiable,
            errors: [],
            type: "Kotlin",
            getUrl: function () {
                return getUrl();
            },
            save: function () {
                instance.content = editor.getText();
                if (project.isUserProject()) {
                    provider.saveFile(url, this);
                } else {
                    project.save();
                }
            },
            onProjectRenamed: function () {
                element.id = getUrl();
            },
            onContentChange: function (text) {
                instance.content = text;
                project.onChange();
            },
            onDelete: function () {
                element.parentElement.removeChild(element);
            },
            rename: function (newName) {
                newName = newName.endsWith(".kt") ? newName : newName + ".kt";
                instance.name = newName;
                element.id = getUrl();
                fileNameElement.innerHTML = newName;
            }
        };

        var provider = (function () {
            provider = new FileProvider();

            provider.onFileRenamed = function(newName){
                instance.rename(newName);
                problemsView.onProjectChange(project);
            };

            provider.onDeleteFile = function(){
                instance.onDelete();
                project.onDeleteFile(instance.getUrl());
            };

            return provider
        })();

        if (element != null) {
            element.id = getUrl();
            element.className = "example-filename";

            var icon = document.createElement("div");
            if (instance.type == "Kotlin") {
                icon.className = "kotlin-file-type-default"
            } else {
                icon.className = "kotlin-file-type-default"
            }
            element.appendChild(icon);


            fileNameElement = document.createElement("div");
            fileNameElement.className = "example-filename-text";
            fileNameElement.innerHTML = instance.name;
            element.appendChild(fileNameElement);

            if (project.isUserProject() && instance.modifiable) {
                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename this file";
                renameImg.onclick = function (event) {
                    renameFileDialog.open(provider.renameFile.bind(null, getUrl()));
                    event.stopPropagation();
                };


                var deleteImg = document.createElement("div");
                deleteImg.className = "delete-img";
                deleteImg.title = "Delete this file";
                deleteImg.onclick = function (event) {
                    provider.deleteFile(instance.getUrl());
                    event.stopPropagation();
                };
                element.appendChild(deleteImg);
                element.appendChild(renameImg);
            }

            element.onclick = function () {
                project.selectFile(instance);
            };
        }

        function getUrl() {
            return project.getUrl() + "&filename=" + instance.name;
        }

        return instance;
    }

    return File;
})();