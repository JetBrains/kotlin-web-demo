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

var AccordionView = (function () {
    function AccordionView(element) {
        var lastSelectedItem = 0;

        var examplesModel = new ExamplesModel();
        var programsModel = new ProgramsModel();
        var examplesView = new ExamplesView(examplesModel);
        var programsView = new ProgramsView(programsModel);


        var instance = {
            saveProgram:function () {
                programsView.saveProgram();
            },
            loadAllContent:function () {
                examplesView.loadAllContent();
            },
            setConfiguration:programsView.setConfiguration,
            onLoadCode:function (example, isProgram) {
            },
            onSaveProgram:function () {
            },
            onDeleteProgram:function () {
            },
            onLoadAllContent:function () {
            },
            onFail:function (exception, messageForStatusBar) {
            }

        };

        examplesView.onAllExamplesLoaded = function () {
            programsView.loadAllContent()
        };
        programsView.onAllProgramsLoaded = function () {
            makeAccordion();
            loadFirstItem();
            instance.onLoadAllContent();
        };


        ProgramsView.setLastSelectedItem = function (item) {
            lastSelectedItem = item;
        };
        ProgramsView.getLastSelectedItem = function () {
            return lastSelectedItem;
        };
        ExamplesView.setLastSelectedItem = function (item) {
            lastSelectedItem = item;
        };
        ExamplesView.getLastSelectedItem = function () {
            return lastSelectedItem;
        };
        ProgramsView.getMainElement = function () {
            return element;
        };
        ExamplesView.getMainElement = function () {
            return element;
        };


        programsModel.onFail = function (data, statusBarMessage) {
            instance.onFail(data, statusBarMessage);
        };
        programsModel.onLoadProgram = function (program) {
            instance.onLoadCode(program, true);
        };
        programsModel.onGeneratePublicLink = function (data) {
            programsView.generatePublicLink(data);
        };
        programsModel.onDeleteProgram = function (data) {
            programsView.deleteProgram(data);
            instance.onDeleteProgram();
        };
        programsModel.onSaveProgram = function (data) {
            programsView.saveProgramWithName(data);
            instance.onSaveProgram();
        };
        programsModel.onAllProgramsLoaded = function (data) {
            programsView.loadAllPrograms(data);
        };

        examplesModel.onLoadExample = function (example) {
            instance.onLoadCode(example, false);
        };
        examplesModel.onAllExamplesLoaded = function (data) {
            examplesView.loadAllExamples(data);
        };

        function makeAccordion() {
            $(".accordionForExamplesAndPrograms").accordion({
                autoHeight:false,
                navigation:true
            }).find('#tools img').click(function (ev) {
                    ev.preventDefault();
                    if (this.id == "saveProgram") {
                        programsView.saveProgram();
                    } else if (this.id == "saveAsProgram") {
                        $("#saveDialog").dialog("open");
                    } else if (this.id == "showInfoAboutLogin") {
                        $("#showInfoAboutLoginDialog").dialog("open");
                    }
                });
        }

        function loadFirstItem() {
            var urlAct = [location.protocol, '//', location.host, "/"].join('');
            var url = document.location.href;
            if (url.indexOf(urlAct) != -1) {
                url = url.substring(url.indexOf(urlAct) + urlAct.length);
                var exampleStr = "?folder=";
                var publicLink = "?publicLink=";
                if (url.indexOf(exampleStr) == 0) {
                    url = url.substring(exampleStr.length);
                    $("#" + getFolderNameByUrl(url)).click();
                    examplesView.loadExample(url);
                } else if (url.indexOf(publicLink) == 0) {
                    programsView.loadProgram(createExampleUrl(url.substring(publicLink.length), "My Programs"));
                } else {
                    examplesView.loadExample("Hello,_world!&name=Simplest_version");
                }
            }
        }

        return instance;
    }



    return AccordionView;
})();
