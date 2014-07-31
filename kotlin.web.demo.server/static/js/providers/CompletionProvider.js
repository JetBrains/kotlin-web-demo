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
 * Date: 3/28/12
 * Time: 6:40 PM
 */

var CompletionProvider = (function () {
    function CompletionProvider() {

        var instance = {
            getCompletion: function (data) {
                getCompletion(data[0], data[1], data[2], data[3], data[4]);
            },
            onLoadCompletion: function (data) {
            },
            onFail: function (error) {
            }
        };

        var isCompletionInProgress = false;

        function getCompletion(dependencies, mode, file, cursorLine, cursorCh) {
            if (!isCompletionInProgress) {
                isCompletionInProgress = true;
                $.ajax({
                    url: generateAjaxUrl("complete", cursorLine + "," + cursorCh + "&runConf=" + dependencies),
                    context: document.body,
                    success: function (data) {
                        isCompletionInProgress = false;
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                instance.onLoadCompletion(data);
                            } else {
                                instance.onFail(data);
                            }
                        } else {
                            instance.onFail("Incorrect data format.");
                        }
                    },
                    dataType: "json",
                    type: "POST",
                    data: {text: file},
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        isCompletionInProgress = false;
                        instance.onFail(textStatus + " : " + errorThrown);
                    }
                });
            }
        }
        return instance;
    }


    return CompletionProvider;
})();