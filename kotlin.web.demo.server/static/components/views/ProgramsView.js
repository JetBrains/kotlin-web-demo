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
            loadAllPrograms:function (data) {
                addAllProgramsInAccordion(data);
            },
            setConfiguration:function (conf) {
                configuration = conf;
            },
            generatePublicLink:function (data) {
                processPublicLink(data);
            },
            deleteProgram:function (data) {
                processDeleteProgram(data);
            },
            saveProgramWithName:function (data) {
                processSaveProgram(data);
            },
            loadAllContent:function () {
                loadAllContent();
            },
            loadProgram:function (url) {
                loadProgram(url);
            },
            saveProgram:function () {
                saveProgram();
            },
            onAllProgramsLoaded:function () {
            }

        };


        $("#saveDialog").dialog({
            modal:"true",
            width:300,
            height:120,
            autoOpen:false,
            buttons:[
                { text:"Save",
                    click:function () {
                        saveAsProgram();
                    }
                },
                { text:"Cancel",
                    click:function () {
                        $(this).dialog("close");
                    }
                }
            ],
            open:function () {
                setTimeout(function () {
                    $("#programName").focus();
                }, 200);
            }
        });

        $("#saveDialog form").submit(function () {
            saveAsProgram();
        });

        $("#showInfoAboutLoginDialog").dialog({
            modal:"true",
            width:300,
            autoOpen:false
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
                            $("a[id='" + ProgramsView.getLastSelectedItem() + "']").attr("class", "");
                            ProgramsView.setLastSelectedItem(createExampleUrl(id, "My Programs"));
                            $("a[id='" + ProgramsView.getLastSelectedItem() + "']").attr("class", "selectedExample");
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
                        document.getElementById(createExampleUrl(data[0].args, "My Programs")).parentNode.parentNode.parentNode.parentNode.removeChild(document.getElementById(createExampleUrl(data[0].args, "My Programs")).parentNode.parentNode.parentNode);
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
            var acc = $(".accordionForExamplesAndPrograms");

            var myProg = document.createElement("h3");
            var innerDiv = document.createElement("div");
            innerDiv.id = "tools";
            var myProgA = document.createElement("a");
            myProgA.href = "#";
            myProgA.id = "My_Programs";

            myProgA.innerHTML = "My Programs";
            innerDiv.appendChild(myProgA);


            if (ProgramsView.isLoggedIn()) {
                var saveImg = document.createElement("img");
                saveImg.src = "/static/icons/save1.png";
                saveImg.id = "saveProgram";
                //todo
                var isMac = false;
                if (!isMac) {
                    saveImg.title = "Save current program (Ctrl + S)";
                } else {
                    saveImg.title = "Save current program (Cmd + S)";
                }
                var saveAsImg = document.createElement("img");
                saveAsImg.src = "/static/icons/saveAs1.png";
                saveAsImg.id = "saveAsProgram";
                saveAsImg.title = "Save current program as ...";
                innerDiv.appendChild(saveAsImg);
                innerDiv.appendChild(saveImg);
            } else {
                var infoImg = document.createElement("img");
                infoImg.src = "/static/images/whatisthis.png";
                infoImg.id = "showInfoAboutLogin";
                infoImg.title = "Help";
                innerDiv.appendChild(infoImg);
            }
            myProg.appendChild(innerDiv);
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
            var table = document.createElement("table");
            var tr = document.createElement("tr");
            var tdIcon = document.createElement("td");
            tdIcon.style.width = "16px";
            var span = document.createElement("div");
            span.className = "bullet";
            if (confType == undefined) {
                span.style.background = "url(/static/icons/text.png) no-repeat";
            } else {
                span.style.background = "url(/static/icons/" + confType + ".png) no-repeat";
            }

            tdIcon.appendChild(span);
            var tdContent = document.createElement("td");
            var contA = document.createElement("a");
            contA.id = id;
            contA.style.cursor = "pointer";
            contA.onclick = function () {
                loadProgram(this.id);
            };
            contA.innerHTML = name;
            tdContent.appendChild(contA);
            var tdDelete = document.createElement("td");
            tdDelete.style.width = "16px";
            var deleteImg = document.createElement("img");
            deleteImg.src = "/static/icons/delete.png";
            deleteImg.title = "Delete this program";
            deleteImg.onclick = function () {
                //in table get td with a element - id of a
                deleteProgram(this.parentNode.parentNode.childNodes[1].childNodes[0].id);
            };
            tdDelete.appendChild(deleteImg);
            var tdLink = document.createElement("td");
            tdLink.style.width = "16px";
            var linkImg = document.createElement("img");
            linkImg.src = "/static/icons/link1.png";
            linkImg.title = "Public link for this program";
            linkImg.onclick = function () {
                //in table get td with a element - id of a
                // structure table-tr-td-a
                generatePublicLink(this.parentNode.parentNode.childNodes[1].childNodes[0].id);
            };
            tdLink.appendChild(linkImg);

            tr.appendChild(tdIcon);
            tr.appendChild(tdContent);
            tr.appendChild(tdLink);
            tr.appendChild(tdDelete);
            table.appendChild(tr);
            return table;
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

                    $("a[id='" + ProgramsView.getLastSelectedItem() + "']").attr("class", "");
                    ProgramsView.setLastSelectedItem(url);
                    $("a[id='" + ProgramsView.getLastSelectedItem() + "']").attr("class", "selectedExample");

                    model.loadProgram(url);
                };
            }(url));
        }

        function saveProgram() {
            if (ProgramsView.isLoggedIn()) {
                if (ProgramsView.getLastSelectedItem() == 0) {
                    $("#saveAsProgram").click();
                    return;
                }
                var folder = getFolderNameByUrl(ProgramsView.getLastSelectedItem());
                if (folder != "My_Programs") {
                    $("#saveAsProgram").click();
                    return;
                }

                model.saveProgram("id=" + ProgramsView.getLastSelectedItem(), Configuration.getStringFromType(configuration.type));

            } else {
                $("#showInfoAboutLogin").click();
            }
        }

        function saveAsProgram() {
            if (ProgramsView.isLoggedIn()) {
                $("#saveDialog").dialog("close");
                var programName = $("#programName").val();
                $("#programName").val("");
                model.saveProgram(programName, Configuration.getStringFromType(configuration.type));
            } else {
                $("#showInfoAboutLogin").click();
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