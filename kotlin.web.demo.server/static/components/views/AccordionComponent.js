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
 generate_public_link
 delete_program
 save_program
 write_exception
 load_example
 */

var AccordionView = (function () {

    var lastSelectedItem = 0;

    var eventHandler = new EventsHandler();

    var examplesModel = new ExamplesModel();
    var examplesView = new ExamplesView();
    var programsView = new ProgramsView();
    var programsModel = new ProgramsModel();

    function AccordionView() {

        var instance = {
            addListener:function (name, f) {
                eventHandler.addListener(name, f);
            },
            fire:function (name, param) {
                eventHandler.fire(name, param);
            },
            getLastSelectedItem:function () {
                return lastSelectedItem;
            },
            setLastSelectedItem:function (item) {
                lastSelectedItem = item;
            },
            saveProgram:function () {
                programsView.saveProgram();
            },
            loadAllContent:function (status, data) {
                if (status) examplesView.loadAllContent();

            },
            changeConfiguration:programsView.changeConfiguration

        };

        examplesView.addListener("load_all_content", programsView.loadAllContent);
        programsView.addListener("load_all_content", function () {
            makeAccordion();
            loadFirstItem();
        });


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


        programsModel.addListener("load_program", function (status, data) {
            eventHandler.fire("load_program", status, data)
        });
        programsModel.addListener("generate_public_link", function (status, data) {
            eventHandler.fire("generate_public_link", status, data)
        });
        programsModel.addListener("delete_program", function (status, data) {
            eventHandler.fire("delete_program", status, data)
        });
        programsModel.addListener("save_program", function (status, data) {
            eventHandler.fire("save_program", status, data)
        });
        programsModel.addListener("write_exception", function (status, data) {
            eventHandler.fire("write_exception", status, data)
        });
        examplesModel.addListener("load_example", function (status, data) {
            eventHandler.fire("load_example", status, data)
        });
        examplesModel.addListener("write_exception", function (status, data) {
            eventHandler.fire("write_exception", status, data)
        });

        examplesModel.addListener("get_all_examples", examplesView.loadAllExamples);
        programsModel.addListener("get_all_programs",  programsView.loadAllPrograms);


//        eventHandler.addListener("get_all_examples", examplesView.loadAllExamples);

//        eventHandler.addListener("get_all_programs", programsView.loadAllPrograms);

        eventHandler.addListener("generate_public_link", programsView.generatePublicLink);
        eventHandler.addListener("delete_program", programsView.processDeleteProgram);
        eventHandler.addListener("save_program", programsView.processSaveProgram);

        return instance;
    }

    function makeAccordion() {
        $("#accordion").accordion({
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

    return AccordionView;
})();


function getNameByUrl(url) {
    var pos = url.indexOf("&name=");
    if (pos != -1) {
        return replaceAll(url.substring(pos + 6), "_", " ");

    }
    return "";
}

function getFolderNameByUrl(url) {
    var pos = url.indexOf("&name=");
    if (pos != -1) {
        return url.substring(0, pos);
    }
    return "";
}

function createExampleUrl(name, folder) {
    return replaceAll(folder, " ", "_") + "&name=" + replaceAll(name, " ", "_");
}