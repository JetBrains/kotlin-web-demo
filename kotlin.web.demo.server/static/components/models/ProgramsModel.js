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
 * To change this template use File | Settings | File Templates.
 */

/* EVENTS:
 load_program
 write_exception
 generate_public_link
 delete_program
 save_program
 get_all_programs
 */

var ProgramsModel = (function () {

    var instance;

    function ProgramsModel() {

        instance = {
            loadProgram:function (url) {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("loadProgram", url),
                    context:document.body,
                    success:function (data) {
                        instance.onLoadProgram(true, data);
                    },
                    dataType:"json",
                    type:"GET",
                    timeout:10000,
                    error:function () {
                        instance.onLoadProgram(false, null);
                    }
                });
            },
            generatePublicLink:function (url) {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("generatePublicLink", url),
                    context:document.body,
                    success:function (data) {
                        instance.onPublicLinkGenerated(true, data);
                    },
                    dataType:"json",
                    type:"GET",
                    timeout:10000,
                    error:function () {
                        instance.onPublicLinkGenerated(false, null);
                    }
                });
            },
            getAllPrograms:function () {
                getAllPrograms();
            },
            deleteProgram:function (name) {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("deleteProgram", name),
                    context:document.body,
                    success:function (data) {
                        instance.onDeleteProgram(true, data);
                    },
                    dataType:"json",
                    type:"GET",
                    timeout:10000,
                    error:function () {
                        instance.onDeleteProgram(false, null);
                    }
                });
            },
            saveProgram:function (id, dependencies) {
                var i = ProgramsModel.getEditorContent();
                var arguments = ProgramsModel.getArguments();
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("saveProgram", id + "&runConf=" + dependencies),
                    success:function (data) {
                        instance.onSaveProgram(true, data);
                    },
                    dataType:"json",
                    type:"POST",
                    data:{text:i, consoleArgs:arguments},
                    timeout:10000,
                    error:function () {
                        instance.onSaveProgram(false, null);
                    }
                });
            },
            onLoadProgram:function (status, data) {
            },
            onPublicLinkGenerated:function (status, data) {
            },
            onDeleteProgram:function (status, data) {
            },
            onSaveProgram:function (status, data) {
            },
            onAllProgramsLoaded:function (status, data) {
            }
        };

        return instance;
    }

    function substringDependencies(dependencies) {
        var pos = dependencies.indexOf(" ");
        if (pos >= 0) {
            return dependencies.substring(0, pos);
        }
        return dependencies;
    }

    ProgramsModel.getEditorContent = function () {
        return ""
    };
    ProgramsModel.getArguments = function () {
        return ""
    };

    function getAllPrograms() {
        $.ajax({
            url:RequestGenerator.generateAjaxUrl("loadProgram", "all"),
            context:document.body,
            success:function (data) {
                instance.onAllProgramsLoaded(true, data);
            },
            dataType:"json",
            type:"GET",
            timeout:10000,
            error:function () {
                instance.onAllProgramsLoaded(false, null);
            }
        });
    }


    return ProgramsModel;
})();