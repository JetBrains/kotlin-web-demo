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

    function FileView(projectView, headerElement, file) {

        var instance = {
            onSelect: function () {
            },
            getProjectView: function () {
                return projectView;
            },
            getFile: function () {
                return file;
            },
            getHeaderElement: function () {
                return headerElement;
            },
            getPublicId: function () {
                return file.publicId;
            },
            canBeSelected: function () {

            },
            fireSelectEvent: function () {
                accordion.selectFile(instance);
            },
            onDelete: function (publicId) {

            }
        };

        var fileNameElement;
//        var file = new File(projectView.getProjectData(), fileData);
        file.onModified = function () {
            $(headerElement).addClass("modified");
            if (accordion.getSelectedFile() == file) {
                accordion.onModifiedSelectedFile();
            }
        };
        file.onUnmodified = function () {
            $(headerElement).removeClass("modified");
            if (accordion.getSelectedFile() == file) {
                accordion.onUnmodifiedSelectedFile();
            }
        };
        var selected = false;
        var renameFileDialog = new InputDialogView("Rename file", "filename", "Rename");
        renameFileDialog.validate = function (newName) {
            if (removeKotlinExtension(file.name) == newName) {
                return {valid: true};
            } else {
                return projectView.validateNewFileName(newName);
            }
        };
        init();


        function init() {
            headerElement.className = "example-filename";

            var icon = document.createElement("div");
            if (file.modifiable) {
                icon.className = "kotlinFileIcon"
            } else {
                icon.className = "unmodifiableKotlinFileIcon"
            }
            headerElement.appendChild(icon);


            fileNameElement = document.createElement("div");
            fileNameElement.className = "example-filename-text";
            fileNameElement.innerHTML = file.name;
            headerElement.appendChild(fileNameElement);

            if (projectView.getType() == ProjectType.USER_PROJECT && file.modifiable) {
                var renameImg = document.createElement("div");
                renameImg.className = "rename-img";
                renameImg.title = "Rename file";
                renameImg.onclick = function (event) {
                    var renameFileFunction = fileProvider.renameFile.bind(null, file.publicId, function (newName) {
                        newName = addKotlinExtension(newName);
                        file.name = newName;
                        fileNameElement.innerHTML = newName;
                    });
                    renameFileDialog.open(renameFileFunction, removeKotlinExtension(file.name));

                    event.stopPropagation();
                };


                var deleteImg = document.createElement("div");
                deleteImg.className = "delete-img";
                deleteImg.title = "Delete this file";
                deleteImg.onclick = function (event) {
                    if (confirm("Delete file " + file.name)) {
                        fileProvider.deleteFile(file.publicId, function () {
                            editor.closeFile();
                            headerElement.parentNode.removeChild(headerElement);
                            instance.onDelete(file.publicId);
                        });
                    }
                    event.stopPropagation();
                };
                headerElement.appendChild(deleteImg);
                headerElement.appendChild(renameImg);
            }

            headerElement.onclick = instance.fireSelectEvent;
        }

        return instance;
    }

    return FileView;
})();