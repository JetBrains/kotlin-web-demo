/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

FileType = {
    KOTLIN_FILE: "KOTLIN_FILE",
    KOTLIN_TEST_FILE: "KOTLIN_TEST_FILE",
    JAVA_FILE: "JAVA_FILE"
};

var FileView = (function () {

    function FileView(projectView, parentNode, file) {

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
            //TODO remove this function
            getWrapper: function (){
                return wrapper;
            },
            getHeaderText: function(){
                return fileNameElement.innerHTML;
            },
            updateName: function(){
                fileNameElement.innerHTML = file.name;
                fileNameElement.title = fileNameElement.innerHTML;
            },
            fireSelectEvent: function () {
                projectView.setSelectedFileView(instance);
                accordion.selectFile(instance);
            }
        };

        var wrapper;
        var headerElement;
        var fileNameElement;
        file.listenableIsModified.addModifyListener(function (e) {
            if(e.newValue) {
                $(headerElement).addClass("modified");
            } else {
                $(headerElement).removeClass("modified");
            }
            if (isSelected()) {
                accordion.onModifiedSelectedFile(file);
            }
        });

        var renameFileDialog = new InputDialogView("Rename file", "File name:", "Rename");
        renameFileDialog.validate = function (newName) {
            if (removeKotlinExtension(file.name) == newName) {
                return {valid: true};
            } else {
                return projectView.validateNewFileName(newName);
            }
        };


        init();
        function init() {
            wrapper = document.createElement("div");
            wrapper.className = "file-header-wrapper";
            wrapper.setAttribute("depth", projectView.getDepth() + 1);
            parentNode.appendChild(wrapper);

            headerElement = document.createElement("div");
            headerElement.className = "file-header";
            wrapper.appendChild(headerElement);

            var icon = document.createElement("div");
            $(icon).addClass("icon").addClass("high-res-icon");
            switch (file.type){
                case FileType.KOTLIN_FILE:
                    $(icon).addClass("kotlin");
                    break;
                case FileType.KOTLIN_TEST_FILE:
                    $(icon).addClass("kotlin-test");
                    break;
                case FileType.JAVA_FILE:
                    $(icon).addClass("java");
                    break;
            }

            if (!file.isModifiable) {
                $(icon).addClass("unmodifiable")
            }
            headerElement.appendChild(icon);


            fileNameElement = document.createElement("div");
            fileNameElement.className = "text";
            fileNameElement.innerHTML = file.name;
            fileNameElement.title = fileNameElement.innerHTML;
            headerElement.appendChild(fileNameElement);
            if(!file.isModifiable){
                $(headerElement).addClass("unmodifiable");
            }

            var actionIconsElement = document.createElement("div");
            actionIconsElement.className = "icons";
            headerElement.appendChild(actionIconsElement);

            if (projectView.getType() == ProjectType.USER_PROJECT) {
                if (file.isModifiable) {
                    var renameImg = document.createElement("div");
                    renameImg.className = "rename icon";
                    renameImg.title = "Rename file";
                    renameImg.onclick = function (event) {
                        var renameFileFunction = fileProvider.renameFile.bind(null, file.id, function(newName){
                            file.name = addKotlinExtension(newName);
                        });
                        file.listenableName.addModifyListener(function (e) {
                            fileNameElement.innerHTML = e.newValue;
                            fileNameElement.title = fileNameElement.innerHTML;
                        });
                        renameFileDialog.open(renameFileFunction, removeKotlinExtension(file.name));
                        event.stopPropagation();

                    };
                    actionIconsElement.appendChild(renameImg);
                }

                var deleteImg = document.createElement("div");
                deleteImg.className = "delete icon";
                deleteImg.title = "Delete this file";
                deleteImg.onclick = function (event) {
                    if (confirm("Delete file " + file.name)) {
                        fileProvider.deleteFile(file, function () {
                            file.project.deleteFile(file);
                            headerElement.parentNode.removeChild(headerElement);
                        });
                    }
                    event.stopPropagation();
                };
                actionIconsElement.appendChild(deleteImg);
            }

            headerElement.onclick = instance.fireSelectEvent;
        }

        function isSelected() {
            return accordion.getSelectedFile() == file;
        }

        return instance;
    }

    return FileView;
})();