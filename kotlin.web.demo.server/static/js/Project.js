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
 * Created by Semyon.Atamas on 8/18/2014.
 */


var Example = (function () {
    function Example(exampleContent, element) {
        var content = exampleContent;
        for(var i = 0; i < content.files.length; i++){
            content.files[i].errors = []
        }

        var selectedFile = null;

        var instance = {
            select: function () {
                problemsView.onExampleChange();
                helpViewForExamples.showHelp(content.help);
                editor.open(content.files[0]);
                argumentsView.val(content.args);
                configurationManager.updateConfiguration(content.confType);
            },
            processHighlightingResult: function(data){
                for(var i = 0; i < content.files.length; i++){
                    content.files[i].errors = data[content.files[i].name];
                }
                editor.updateHighlighting();
            },
            getModifiableContent: function () {
                return {
                    args: content.args,
                    confType: content.confType,
                    name: content.name,
                    parent: content.parent,
                    files: content.files.filter(function (file) {
                        return file.modifiable;
                    })
                };
            },
            getArguments: function () {
                return content.args;
            },
            getURL: function () {
                return replaceAll(content.parent, " ", "_") + "&name=" + replaceAll(content.name, " ", "_");
            },
            errorsExists: function(){
                for(var i = 0; i < content.files.length; i++){
                    var errors = content.files[i].errors;
                    for(var j = 0; j < errors.length; j++){
                        if(errors[j].severity == "ERROR"){
                            return true;
                        }
                    }
                }
                return false;
            },
            save: function(){
                content.confType = configurationManager.getType();
                content.args = argumentsView.val();
            },
            getFiles: function(){
                return content.files;
            }

        };


        function updateExampleFiles() {
            var exampleContentElement = document.getElementById(instance.getURL() + "_content");
            exampleContentElement.innerHTML = "";


            for (var i = 0; i < content.files.length; i++){
                var file = content.files[i];

                var filenameDiv = document.createElement("div");
                filenameDiv.id = getFilenameURL(file.name);
                filenameDiv.className = "example-filename";

                var icon = document.createElement("div");
                if(file.type == "Kotlin"){
                    icon.className = "kotlin-file-type-default"
                }
                filenameDiv.appendChild(icon);



                var fileNameSpan = document.createElement("div");
                fileNameSpan.style.paddingLeft = "19px";
                fileNameSpan.innerHTML = file.name;
                filenameDiv.appendChild(fileNameSpan);
                filenameDiv.onclick = (function (file) {
                    return function () {
                        selectFile(file);
                    }
                })(file);

                exampleContentElement.appendChild(filenameDiv);
            }

        }

        updateExampleFiles();


        function selectFile(file) {
            if (selectedFile != null) {
                document.getElementById(getFilenameURL(selectedFile.name)).className = "example-filename";
            }
            selectedFile = file;
            document.getElementById(getFilenameURL(file.name)).className = "example-filename-selected";
            editor.open(selectedFile);
        }

        selectFile(content.files[0]);

        function getFilenameURL(filename) {
            return instance.getURL() + "&filename=" + filename.replace(/ /g, "_");
        }

        return instance;
    }

    return Example;
})
();