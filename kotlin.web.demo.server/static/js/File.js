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


    function File(project, name, content, modifiable, publicId, element) {
        var fileNameElement;
        var isContentChanged = false;

        var instance = {
            name: name,
            content: content,
            modifiable: modifiable,
            errors: [],
            type: "Kotlin",
            publicId: publicId,
            getUrl: function () {
                return getUrl();
            },
            save: function () {
                isContentChanged = false;
                instance.content = editor.getText();
                if (project.getType() == ProjectType.USER_PROJECT) {
                    provider.saveFile(publicId, this);
                } else {
                    project.save();
                }
            },
            open: function () {
                editor.open(instance);
                if (publicId.startsWith("folder")) {
                    window.history.pushState("", "", "?" + publicId)
                } else {
                    window.history.pushState("", "", "?id=" + publicId)
                }
            },
            onContentChange: function (text) {
                isContentChanged = true;
                instance.content = text;
                project.onChange();
            },
            onDelete: function () {
                element.parentElement.removeChild(element);
            },
            rename: function (newName) {
                newName = newName.endsWith(".kt") ? newName : newName + ".kt";
                instance.name = newName;
                fileNameElement.innerHTML = newName;
            },
            isContentChanged: function () {
                return isContentChanged;
            }
        };

        var renameFileDialog = new InputDialogView("Rename file", "filename", "Rename");
        renameFileDialog.verify = project.verifyNewFilename;

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

            if (project.getType() == ProjectType.USER_PROJECT && instance.modifiable) {
                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename this file";
                renameImg.onclick = function (event) {
                    renameFileDialog.open(provider.renameFile.bind(null, publicId), getNameWithoutExtension());
                    event.stopPropagation();
                };


                var deleteImg = document.createElement("div");
                deleteImg.className = "delete-img";
                deleteImg.title = "Delete this file";
                deleteImg.onclick = function (event) {
                    if(confirm("Delete file " + instance.name)){
                        provider.deleteFile(publicId);
                    }
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
            return "file_" + publicId;
        }

        function getNameWithoutExtension(){
            return instance.name.slice(0, -3);
        }

        return instance;
    }

    return File;
})();