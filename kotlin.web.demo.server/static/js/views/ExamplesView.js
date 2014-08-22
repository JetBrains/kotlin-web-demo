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
        var acc = $("#examples-list");
        var downloadedExamples = new Object();//map exampleID -> example
        var selectedExample = null;

        var confirmDialog = new ConfirmDialog();
        var model = examplesModel;

        var instance = {
            loadAllExamples: function (data) {
                addAllExamplesInAccordion(data);
                instance.onAllExamplesLoaded();
            },
            onLoadExample: function (exampleContent) {
                onLoadExample(exampleContent);
            },
            loadAllContent: function () {
                loadAllContent();
            },
            loadExample: function (url) {
                model.loadExample(url);
            },
            onAllExamplesLoaded: function () {
            },
            getSelectedExample: function () {
                return selectedExample;
            }
        };

        function loadAllContent() {
            ExamplesView.getMainElement().html("");
//            var acc = document.createElement("div");
//            acc.className = "accordionForExamplesAndPrograms";
//            ExamplesView.getMainElement().append(acc);
            model.getAllExamples();
        }

        function onLoadExample(exampleContent) {
            var example = new Example(exampleContent);
            downloadedExamples[example.getURL()] = example;
            showExample(example.getURL());
        }


        function addFolder(folderObj) {
            var folder = document.createElement("h3");
            var folderDiv = document.createElement("div");
            folder.className = "examples-folder-name";

            folderDiv.innerHTML = folderObj.name;
            folderDiv.className = "folder-name-div";

            folder.appendChild(folderDiv);
            acc.append(folder);
            var cont = document.createElement("div");
            var i = 0;
            while (folderObj.examplesOrder[i] != undefined) {
                addExample(folderObj.name, cont, folderObj.examplesOrder[i]);
                i++;
            }

            acc.append(cont);
        }

        function addExample(folder, cont, name) {
            var file = document.createElement("h4");
            file.className = "examples-project-name";
            var img = document.createElement("div");
            img.className = "arrow";
            file.appendChild(img);
            file.id = createExampleUrl(name, folder) + "_header";


            var nameSpan = document.createElement("span");
            nameSpan.id = createExampleUrl(name, folder);
            nameSpan.className = "file-name-span";
            nameSpan.style.cursor = "pointer";
            nameSpan.onclick = function () {
                if (downloadedExamples[this.id] == undefined) {
                    model.loadExample(this.id);
                } else {
                    showExample(this.id)
                }
            };
            nameSpan.innerHTML = name;

            file.appendChild(nameSpan);

            cont.appendChild(file);

            var exampleContent = document.createElement("div");
            exampleContent.id = createExampleUrl(name, folder) + "_content";
            cont.appendChild(exampleContent);
        }

        function addAllExamplesInAccordion(data) {

            var i = 0;
            while (data[i] != undefined) {
                addFolder(data[i]);
                i++;
            }
        }

        function showExample(url) {
            if (downloadedExamples[url] != selectedExample) {
                if(selectedExample != null) {
                    selectedExample.save();
                    var element = document.getElementById(selectedExample.getURL() + "_header");
                    element.className = "examples-project-name";
                    $(element).next().slideUp();
                }
                selectedExample = downloadedExamples[url];
                element = document.getElementById(selectedExample.getURL() + "_header");
                element.className = "expanded-project-name";
                $(element).next().slideDown();
                selectedExample.select();
            } else {
                var element = document.getElementById(selectedExample.getURL() + "_header");
                if (element.className.indexOf("expanded-project-name") > -1) {
                    element.className = "current-project-name";
                    $(element).next().slideUp()
                } else {
                    element.className = "expanded-project-name";
                    $(element).next().slideDown()
                }
            }

//            $("span[id='" + ExamplesView.getLastSelectedItem() + "']").parent().attr("class", "selected-example");
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