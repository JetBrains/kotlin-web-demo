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
 * Created by Semyon.Atamas on 9/25/2014.
 */


var FileView = (function () {

    function FileView(project, name, publicId, headerElement, /*nullable*/fileData) {

        var instance = {
            deselect: function () {
                if (selected) {
                    selected = false;
                    $(headerElement).removeClass("selected");
                    editor.closeFile();
                }
            },
            select: function () {
                if (instance.canBeSelected()) {
                    selected = true;
                    headerElement.className += " selected";
                    editor.open(file);
                }
            },
            save: function () {
                instance.content = editor.getText();
                if (project.getType() == ProjectType.USER_PROJECT) {
                    provider.saveFile(publicId, fileData);
                } else {
                    project.save();
                }
            },
            getFileData: function () {
                return file;
            },
            getPublicId: function () {
                return publicId;
            },
            canBeSelected: function () {

            },
            onHeaderClick: function (publicId) {

            },
            onDelete: function (publicId) {

            }
        };

        var fileNameElement;
        var file = new FileData(fileData);
        var selected = false;
        var renameFileDialog = new InputDialogView("Rename file", "filename", "Rename");
        renameFileDialog.verify = project.verifyNewFilename;
        init();

        function onDeleteFile() {
            instance.deselect();
            headerElement.parentNode.removeChild(headerElement);
            instance.onDelete(publicId);
        }

        function onFileRenamed(newName) {
            newName = newName.endsWith(".kt") ? newName : newName + ".kt";
            file.name = newName;
            fileNameElement.innerHTML = newName;
        }

        function init() {
            headerElement.className = "example-filename";

            var icon = document.createElement("div");
            if (instance.type == "Kotlin") {
                icon.className = "kotlin-file-type-default"
            } else {
                icon.className = "kotlin-file-type-default"
            }
            headerElement.appendChild(icon);


            fileNameElement = document.createElement("div");
            fileNameElement.className = "example-filename-text";
            fileNameElement.innerHTML = name;
            headerElement.appendChild(fileNameElement);

            if (project.getType() == ProjectType.USER_PROJECT && file.modifiable) {
                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename file";
                renameImg.onclick = function (event) {
                    renameFileDialog.open(fileProvider.renameFile.bind(null, publicId, onFileRenamed),
                        fileData.name.endsWith(".kt") ? fileData.name.substring(0, fileData.name.length - 3) : fileData.name);
                    event.stopPropagation();
                };


                var deleteImg = document.createElement("div");
                deleteImg.className = "delete-img";
                deleteImg.title = "Delete this file";
                deleteImg.onclick = function (event) {
                    if (confirm("Delete file " + name)) {
                        fileProvider.deleteFile(publicId, onDeleteFile);
                    }
                    event.stopPropagation();
                };
                headerElement.appendChild(deleteImg);
                headerElement.appendChild(renameImg);
            }

            headerElement.onclick = function () {
                instance.onHeaderClick(publicId);
            };
        }

        return instance;
    }

    return FileView;
})();