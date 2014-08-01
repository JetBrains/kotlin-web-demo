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


var ProgramsView = (function () {
    function ProgramsView(programsModel) {

        var model = programsModel;

        var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.type.JAVA);
        var BEFORE_DELETE_PROGRAM = "Do you really want to delete the program?";

        var confirmDialog = new ConfirmDialog();

        var instance = {
            loadAllPrograms: function (data) {
                addAllProgramsInAccordion(data);
            },
            setConfiguration: function (conf) {
                configuration = conf;
            },
            generatePublicLink: function (data) {
                processPublicLink(data);
            },
            deleteProgram: function (data) {
                processDeleteProgram(data);
            },
            saveProgramWithName: function (data) {
                processSaveProgram(data);
            },
            loadAllContent: function () {
                loadAllContent();
            },
            loadProgram: function (url) {
                loadProgram(url);
            },
            saveProgram: function () {
                saveProgram();
            },
            saveAsProgram: function(){
                saveAsProgram();
            },
            onAllProgramsLoaded: function () {
            }

        };


        $("#save-dialog").dialog({
            modal: "true",
            width: 380,
            autoOpen: false,
            buttons: [
                { text: "Save",
                    click: function () {
                        saveAsProgram();
                    }
                },
                { text: "Cancel",
                    click: function () {
                        $(this).dialog("close");
                    }
                }
            ],
            open: function () {
                setTimeout(function () {
                    $("#programName").focus();
                }, 200);
            }
        });

        $("#save-dialog form").submit(function () {
            saveAsProgram();
        });

        $("#login-dialog").dialog({
            modal: "true",
            width: 300,
            autoOpen: false
        });

        function processSaveProgram(data) {
            if (data != undefined && data != null) {
                var i = 0;
                while (data[i] != undefined) {
                    if (data[i].type == "exception") {
                        //todo eventHandler.fire("write_exception", data);
                    } else if (data[i].type == "programId") {
                    } else {
                        var pos = data[i].text.indexOf("&id=");
                        if (pos > 0) {
                            var name = replaceAll(data[i].text.substring(0, pos), "%20", " ");
                            var id = data[i].text.substring(pos + 4);

                            $("div#myprogramscontent").append(createProgramListElement(createExampleUrl(id, "My Programs"), name,
                                Configuration.getStringFromType(configuration.type)));
                            $("a#My_Programs").click();
                            $("span[id='" + ProgramsView.getLastSelectedItem() + "']").parent().parent().attr("class", "examples-file-name");
                            ProgramsView.setLastSelectedItem(createExampleUrl(id, "My Programs"));
                            $("span[id='" + ProgramsView.getLastSelectedItem() + "']").parent().parent().attr("class", "selected-example");
                        }
                    }
                    i++;
                }
            }
        }

        function processDeleteProgram(data) {
            if (data != null && data != undefined) {
                if (data[0] != null && data[0] != undefined) {
                    if (data[0].type == "exception") {
                        //TODO eventHandler.fire("write_exception", data);
                    } else {
                        elem = document.getElementById("bullet"+createExampleUrl(data[0].args, "My Programs"));
                        elem.parentNode.removeChild(elem);
                    }
                }
            }
        }

        function processPublicLink(data) {
            if (data != null && data != undefined) {
                if (data[0] != null && data[0] != undefined) {
                    if (data[0].type == "exception") {
                        //TODO eventHandler.fire("write_exception", data);
                    } else {
                        var programId = data[0].text.substring(data[0].text.indexOf("publicLink=") + 11);
                        var name = createExampleUrl(programId, "My Programs");
                        var displayedName = $("a[id='" + name + "']").html();
                        var tweetHref = escape("#Solved " + displayedName + " " + data[0].text + " #Kotlin");
                        $("a[id='" + name + "']").parent("td").parent("tr").parent("table").after("<div class=\"toolbox\" id=\"pld" + name + "\" style=\"display:block;\"><div align=\"center\"><div class=\"fixedpage\"><div class=\"publicLinkHref\" id=\"pl" + name + "\"></div><a target=\"_blank\" href=\"http://twitter.com/home?status=" + tweetHref + "\"><img class=\"tweetImg\" src=\"/static/images/social/twitter.png\"/></a><img class=\"closePopup\" id=\"cp" + name + "\" src=\"/static/icons/close.png\" title=\"Close popup\"></span></div></div></div>");
                        $("div[id='pl" + name + "']").html(data[0].text);
                        $("img[id='cp" + name + "']").click(function () {
                            $("div[id='pld" + name + "']").remove("div");
                        });

                    }
                }
            }
        }

        function loadAllContent() {
            var acc = $("#examples-list");

            var myProg = document.createElement("h3");
            myProg.className = "examples-folder-name";
            myProg.innerHTML = "My programs";


            if (!ProgramsView.isLoggedIn()){
                myProg.style.color = "rgba(0,0,0,0.5)";
                $("#save").css("opacity", "0.3")
                    .attr("title", "Save your program(you must be logged in)");
                var login_link = document.createElement("span");
                login_link.id = "login-link";
                login_link.className = "login-link";
                login_link.innerHTML = "(please log in)";
                myProg.appendChild(login_link);
            }


            acc.append(myProg);
            var myProgCont = document.createElement("div");
            myProgCont.id = "myprogramscontent";
            acc.append(myProgCont);
            if (ProgramsView.isLoggedIn()) {
                model.getAllPrograms();
            } else {
                instance.onAllProgramsLoaded();
            }


        }

        function addAllProgramsInAccordion(data) {
            var i = 0;
            var cont = document.getElementById("myprogramscontent");
            while (data[i] != undefined) {
                $("#myprogramscontent").append(createProgramListElement(createExampleUrl(data[i].id, "My Programs"), replaceAll(data[i].name, "%20", " "), data[i].confType));
                i++;
            }
            instance.onAllProgramsLoaded();
        }

        function createProgramListElement(id, name, confType) {
            var file = document.createElement("div");
            file.className = "examples-file-name";
            var img = document.createElement("div");
            img.className = "right-arrow";
            file.appendChild(img);
            file.id = "bullet" + id;

            var name_div = document.createElement("div");
            var name_span = document.createElement("span");
            name_span.id = id;
            name_span.style.cursor = "pointer";
            name_span.onclick = function () {
                loadProgram(this.id);
            };
            name_span.innerHTML = name;
            name_span.className = "file-name-span";

            var deleteImg = document.createElement("div");
            deleteImg.className = "delete-img";
            deleteImg.title = "Delete this program";
            deleteImg.onclick = function () {
                deleteProgram(this.parentNode.childNodes[1].id);
            };

            name_div.appendChild(deleteImg);
            name_div.appendChild(name_span);
            file.appendChild(name_div);
            return file;
        }

        function deleteProgram(name) {
            if (confirm(BEFORE_DELETE_PROGRAM)) {
                if (ProgramsView.getLastSelectedItem() == name) {
                    ProgramsView.setLastSelectedItem("");
                }
                model.deleteProgram(name);
            }
        }

        function generatePublicLink(name) {
            model.generatePublicLink(name);
        }

        function loadProgram(url) {
            confirmDialog.open(function (url) {
                return function () {
                    if (ProgramsView.getLastSelectedItem() == url) {
                        return;
                    }

                    $("span[id='" + ProgramsView.getLastSelectedItem() + "']").parent().parent().attr("class", "examples-file-name");
                    ProgramsView.setLastSelectedItem(url);
                    $("span[id='" + ProgramsView.getLastSelectedItem() + "']").parent().parent().attr("class", "selected-example");

                    model.loadProgram(url);
                };
            }(url));
        }

        function saveProgram() {
            if (ProgramsView.isLoggedIn()) {
                if (ProgramsView.getLastSelectedItem() == 0) {
                    $("#save-dialog").dialog("open");
                    return;
                }
                var folder = getFolderNameByUrl(ProgramsView.getLastSelectedItem());
                if (folder != "My_Programs") {
                    $("#saveAsProgram").click();
                    return;
                }

                model.saveProgram("id=" + ProgramsView.getLastSelectedItem(), Configuration.getStringFromType(configuration.type));

            } else {
                $("#login-dialog").dialog("open");
            }
        }

        function saveAsProgram() {
            if (ProgramsView.isLoggedIn()) {
                $("#save-dialog").dialog("close");
                var programName = $("#program-name-input").val();
                $("#program-name-input").val("");
                model.saveProgram(programName, Configuration.getStringFromType(configuration.type));
            } else {
                $("#login-dialog").dialog("open");
            }
        }

        return instance;
    }

    ProgramsView.setLastSelectedItem = function () {

    };
    ProgramsView.getLastSelectedItem = function () {

    };
    ProgramsView.getMainElement = function () {

    };

    ProgramsView.isLoggedIn = function () {
        return false;
    };

    return ProgramsView;
})();