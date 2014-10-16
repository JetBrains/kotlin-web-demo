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
 * Created by Semyon.Atamas on 8/26/2014.
 */


var HeadersProvider = (function () {

    function HeadersProvider() {

        var instance = {
            getAllHeaders: function () {
                getAllHeaders();
            },
            getInfoAboutProject: function (publicId) {
                getInfoAboutProjectHeader(publicId);
            },
            onHeadersLoaded: function () {

            },
            onProjectInfoLoaded: function (data) {

            },
            onFail: function () {
            }
        };

        function getAllHeaders() {
            $.ajax({
                url: generateAjaxUrl("loadHeaders"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            var orderedFolderNames = data.orderedFolderNames;
                            var foldersContent = {};

                            for (var i = 0; i < orderedFolderNames.length; ++i) {
                                foldersContent[orderedFolderNames[i]] = [];
                                var orderedExampleNames = data[orderedFolderNames[i]];

                                for (var j = 0; j < orderedExampleNames.length; ++j) {
                                    foldersContent[orderedFolderNames[i]].push({
                                        name: orderedExampleNames[j],
                                        type: ProjectType.EXAMPLE,
                                        publicId: createExampleUrl(orderedExampleNames[j], orderedFolderNames[i])
                                    });
                                }
                            }

                            data.orderedFolderNames.push("My programs");
                            data.orderedFolderNames.push("Public links");

                            if (loginView.isLoggedIn()) {
                                foldersContent["My programs"] = data["My programs"];
                                for (var i = 0; i < foldersContent["My programs"].length; ++i) {
                                    foldersContent["My programs"][i].type = ProjectType.USER_PROJECT;
                                }
                            } else {
                                foldersContent["My programs"] = [];
                            }

                            if (localStorage.getItem("publicLinks") != null) {
                                foldersContent["Public links"] = JSON.parse(localStorage.getItem("publicLinks"));
                            } else {
                                foldersContent["Public links"] = [];
                            }

                            instance.onHeadersLoaded(orderedFolderNames, foldersContent);
                        } else {
                            instance.onFail(data, statusBarView.statusMessages.load_examples_fail);
                        }
                    } else {
                        instance.onFail("Incorrect data format.", statusBarView.statusMessages.load_examples_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.load_examples_fail);
                }
            });
        }

        function getInfoAboutProjectHeader(publicId) {
            $.ajax({
                url: generateAjaxUrl("loadProjectInfoByFileId"),
                success: function (data) {
                    instance.onProjectInfoLoaded(data);
                },
                type: "GET",
                timeout: 10000,
                dataType: "json",
                data: {
                    id: publicId
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, statusBarView.statusMessages.save_program_fail);
                }
            })
        }

        return instance;
    }

    return HeadersProvider;
})();