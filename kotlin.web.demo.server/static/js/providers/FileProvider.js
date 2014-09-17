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

var FileProvider = (function () {

    function FileProvider() {
        var instance = {
            renameFile: function (url, newName) {
                renameFile(url, newName)
            },
            saveFile: function (url, data) {
                saveFile(url, data);
            },
            deleteFile: function (url) {
                deleteFile(url);
            },
            onFileRenamed: function(newName){
            },
            onDeleteFile: function () {

            }
        };



        function renameFile(url, newName) {
            $.ajax({
                url: generateAjaxUrl("renameFile", url),
                success: function(){instance.onFileRenamed(newName)},
                type: "POST",
                timeout: 10000,
                data: {newName: newName}
            })
        }

        function deleteFile(url) {
            $.ajax({
                url: generateAjaxUrl("deleteFile", url),
                context: document.body,
                success: function () {
                    instance.onDeleteFile(url);
                },
                type: "POST",
                timeout: 10000
//                error: function (jqXHR, textStatus, errorThrown) {
//                    project.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
//                }
            });
        }

        function saveFile(url, data) {
            $.ajax({
                url: generateAjaxUrl("saveFile", url),
                type: "POST",
                timeout: 10000,
                data: {file: JSON.stringify(data)}
//                error: function (jqXHR, textStatus, errorThrown) {
//                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
//                }
            })
        }

        return instance;
    }

    return FileProvider;
})();