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
 * Date: 3/30/12
 * Time: 3:37 PM
 */

var ProgramsModel = (function () {

    function ProgramsModel() {

        var instance = {
            loadProject: function (url) {
                $.ajax({
                    url: generateAjaxUrl("loadProject", url),
                    context: document.body,
                    success: function (data) {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                instance.onLoadProject(data);
                            } else {
                                instance.onFail(data, ActionStatusMessages.load_program_fail);
                            }
                        } else {
                            instance.onFail("Incorrect data format.", ActionStatusMessages.load_program_fail);
                        }
                    },
                    dataType: "json",
                    type: "GET",
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
                    }
                });
            },
            generatePublicLink: function (url) {
                $.ajax({
                    url: generateAjaxUrl("generatePublicLink", url),
                    context: document.body,
                    success: function (data) {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                instance.onGeneratePublicLink(data);
                            } else {
                                instance.onFail(data, ActionStatusMessages.generate_link_fail);
                            }
                        } else {
                            instance.onFail("Incorrect data format.", ActionStatusMessages.generate_link_fail);
                        }
                    },
                    dataType: "json",
                    type: "GET",
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.generate_link_fail);
                    }
                });
            },
            getAllPrograms: function () {
                getAllPrograms();
            },
            deleteProgram: function (projectName, fileName) {
                $.ajax({
                    url: generateAjaxUrl("deleteProgram", projectName + "&filename=" + fileName),
                    context: document.body,
                    success: function (data) {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                instance.onDeleteProgram(data);
                            } else {
                                instance.onFail(data, ActionStatusMessages.delete_program_fail);
                            }
                        } else {
                            instance.onFail("Incorrect data format.", ActionStatusMessages.delete_program_fail);
                        }
                    },
                    dataType: "json",
                    type: "GET",
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.delete_program_fail);
                    }
                });
            },
            saveProgram: function (id, dependencies) {
                var i = ProgramsModel.getEditorContent();
                var arguments = ProgramsModel.getArguments();
                $.ajax({
                    url: generateAjaxUrl("saveProgram", id + "&runConf=" + dependencies),
                    success: function (data) {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                instance.onSaveProgram(data);
                            } else {
                                instance.onFail(data, ActionStatusMessages.save_program_fail);
                            }
                        } else {
                            instance.onFail("Incorrect data format.", ActionStatusMessages.save_program_fail);
                        }
                    },
                    dataType: "json",
                    type: "POST",
                    data: {text: i, consoleArgs: arguments},
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    }
                });
            },
            addNewProject: function(name){
                $.ajax({
                    url: generateAjaxUrl("addProject", name),
                    success: function(){
                        accordion.addNewProject(name);
                    },
                    type: "POST",
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    }
                })
            },
            addNewFile: function(projectName, filename){
                $.ajax({
                    url: generateAjaxUrl("addFile", projectName + "&filename=" + filename),
                    success: function(){
                        accordion.addNewFile(projectName, filename);
                    },
                    type: "POST",
                    timeout: 10000,
                    error: function (jqXHR, textStatus, errorThrown) {
                        instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    }
                })
            },
            onLoadProject: function (data) {
            },
            onGeneratePublicLink: function (data) {
            },
            onDeleteProgram: function (data) {
            },
            onSaveProgram: function (data) {
            },
            onAllProgramsLoaded: function (data) {
            },
            onFail: function (exception, statusBarMessage) {
            }
        };

        function getAllPrograms() {
            $.ajax({
                url: generateAjaxUrl("loadProject", "all"),
                context: document.body,
                success: function (data) {
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            instance.onAllProgramsLoaded(data);
                        } else {
                            instance.onFail(data, ActionStatusMessages.load_programs_fail);
                        }
                    } else {
                        instance.onFail("Incorrect data format.", ActionStatusMessages.load_programs_fail);
                    }
                },
                dataType: "json",
                type: "GET",
                timeout: 10000,
                error: function (jqXHR, textStatus, errorThrown) {
                    instance.onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_programs_fail);
                }
            });
        }


        return instance;
    }

    ProgramsModel.getEditorContent = function () {
        return ""
    };
    ProgramsModel.getArguments = function () {
        return ""
    };


    return ProgramsModel;
})();