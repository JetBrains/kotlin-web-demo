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


var ExamplesView = (function () {
    function ExamplesView(examplesModel) {
        var confirmDialog = new ConfirmDialog();
        var model = examplesModel;

        var instance = {
            loadAllExamples: function (data) {
                addAllExamplesInAccordion(data);
                instance.onAllExamplesLoaded();
            },
            processLoadExample: function (status, data) {
                if (status) {
                    loadExampleSuccess(data);
                }
            },
            loadAllContent: function () {
                loadAllContent();
            },
            loadExample: function (url) {
                loadExample(url);
            },
            onAllExamplesLoaded: function () {
            }
        };

        function loadAllContent() {
            ExamplesView.getMainElement().html("");
//            var acc = document.createElement("div");
//            acc.className = "accordionForExamplesAndPrograms";
//            ExamplesView.getMainElement().append(acc);
            model.getAllExamples();
        }

        function addAllExamplesInAccordion(data) {
            var acc = $("#examples-list");
            var i = 0;
            while (data[i] != undefined) {
                var lastFolderName;
                var ids = [];
                if (data[i].type == "folder") {
                    var folder = document.createElement("h3");
                    var folderDiv = document.createElement("div");
                    folder.className = "examples-folder-name";

                    folderDiv.innerHTML = data[i].text;
                    folderDiv.className = "folder-name-div";
                    lastFolderName = data[i].text;
                    folder.appendChild(folderDiv);
                    acc.append(folder);
                    var cont = document.createElement("div");
                }
                if (data[i].type == "content") {
                    var file = document.createElement("div");
                    file.className = "examples-file-name";
                    var img = document.createElement("div");
                    img.className = "right-arrow";
                    file.appendChild(img);
                    file.id = "bullet" + replaceAll(data[i].text, " ", "_");

                    var spanDiv = document.createElement("div");
                    spanDiv.className = "file-name-span";

                    var name = document.createElement("span");
                    name.id = createExampleUrl(data[i].text, lastFolderName);
                    name.style.cursor = "pointer";
                    name.onclick = function () {
                        loadExample(this.id);
                    };
                    name.innerHTML = data[i].text;

                    spanDiv.appendChild(name);
                    file.appendChild(spanDiv);

                    cont.appendChild(file);
                }
                acc.append(cont);

                i++;
            }
        }

        function loadExample(url) {
            confirmDialog.open(function (url) {
                return function () {
                    if (ExamplesView.getLastSelectedItem() == url) {
                        return;
                    }

                    var selecteExample = ExamplesView.getLastSelectedItem();
                    if (selecteExample != 0) {
                        $("span[id='" + selecteExample +"']").parent().parent().attr("class", "examples-file-name");
                    }
                    ExamplesView.setLastSelectedItem(url);
                    $("span[id='" + ExamplesView.getLastSelectedItem() + "']").parent().parent().attr("class", "selected-example");

                    model.loadExample(url);
                };
            }(url));
        }

        function loadExampleSuccess(data) {
        }


        function generatePublicLinkForExample(name) {
            if ($("div[id='pld" + name + "']").length <= 0) {
                var url = [location.protocol, '//', location.host, "/"].join('');
                var href = url + "?folder=" + name;
                $("a[id='" + name + "']").parent("td").parent("tr").parent("table").after("<div class=\"toolbox\" id=\"pld" + name + "\" style=\"display:block;\"><div align=\"center\"><div class=\"fixedpage\"><div class=\"publicLinkHref\" id=\"pl" + name + "\"></div><img class=\"closePopup\" id=\"cp" + name + "\" src=\"/static/icons/close.png\" title=\"Close popup\"></span></div></div></div>");
                $("div[id='pl" + name + "']").html(href);
                $("img[id='cp" + name + "']").click(function () {
                    $("div[id='pld" + name + "']").remove("div");
                });
            }
        }


        return instance;
    }

    ExamplesView.setLastSelectedItem = function () {
    };
    ExamplesView.getLastSelectedItem = function () {
    };
    ExamplesView.getMainElement = function () {
    };


    return ExamplesView;
})();