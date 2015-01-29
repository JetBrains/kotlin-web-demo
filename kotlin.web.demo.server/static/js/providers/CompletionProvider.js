/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/29/12
 * Time: 1:56 PM
 */

var CompletionProvider = (function () {

    function CompletionProvider() {

        var instance = {
            onSuccess: function (data) {

            },
            onFail: function (error) {

            },
            getCompletion: function (project, filename, cursor, callback) {
                getCompletion(project, filename, cursor, callback);
            }
        };

        var isLoadingCompletion = false;

        function getCompletion(project, filename, cursor, callback) {
            if (!isLoadingCompletion) {
                isLoadingCompletion = true;
                $.ajax({
                    url: generateAjaxUrl("complete"),
                    context: document.body,
                    success: function (data) {
                        isLoadingCompletion = false;
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                instance.onSuccess(data);
                                callback(data);
                            } else {
                                instance.onFail(data);
                            }
                        } else {
                            instance.onFail("Incorrect data format.");
                        }
                    },
                    dataType: "json",
                    timeout: 10000,
                    type: "POST",
                    data: {
                        project: JSON.stringify(project),
                        filename: filename,
                        line: cursor.line,
                        ch: cursor.ch
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        isLoadingCompletion = false;
                        instance.onFail(textStatus + " : " + errorThrown);
                    }
                });
            }
        }

        return instance;
    }


    return CompletionProvider;
})();