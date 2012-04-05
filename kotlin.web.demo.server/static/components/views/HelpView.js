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


var HelpView = (function () {

    var helpForExamples = [];
    var helpForWords = [];

    var model = new HelpModel();

    var isShortcutsShow = true;

    function HelpElement(name, text) {
        this.name = name;
        this.text = text;
    }

    function HelpView() {
        model.getAllHelpForExamples();
        model.getAllHelpForWords();

        var instance = {

            loadExample:function (status, example) {
                if (status) {
                    loadHelpForExample(example.name);
                }
            },
            helpForExamplesLoaded:function (status, data) {
                if (status) {
                    if (data != null) {
                        var i = 0;
                        while (typeof data[i] != "undefined") {
                            var helpEl = new HelpElement(data[i].name, data[i].text);
                            helpForExamples.push(helpEl);
                            i++;
                        }
                    }
                }
            },
            helpForWordsLoaded:function (status, data) {
                if (status) {
                    if (data != null) {
                        var i = 0;
                        while (typeof data[i] != "undefined") {
                            var helpEl = new HelpElement(data[i].name, data[i].text);
                            helpForWords.push(helpEl);
                            i++;
                        }
                    }
                }
            },
            loadHelpForWord:function (word) {
                loadHelpForWord(word);
            },
            processCursorActivity:function (status, data) {
                if (status) loadHelpForWord(data[1]);
            }
        };

        $(".toggleShortcuts").click(function () {
            $("#help3").toggle();
            if (isShortcutsShow) {
                isShortcutsShow = false;
                document.getElementById("toggleShortcutsButton").src = "/static/images/toogleShortcutsOpen.png";
            } else {
                isShortcutsShow = true;
                document.getElementById("toggleShortcutsButton").src = "/static/images/toogleShortcuts.png";

            }
        });

        $("#help3").toggle(true);

        if (navigator.appVersion.indexOf("Mac") != -1) {
            var text = $("#help3").html();
            text = text.replace("F9", "R");
            $("#help3").html(text.replace("F9", "R"));
        }

        return instance;
    }

    var isHelpForWordLoaded = false;

    function loadHelpForWord(word) {
        isHelpForWordLoaded = false;
        forEachInArrayWithArgs(helpForWords, word, compareHelpForWords);
        if (!isHelpForWordLoaded) {
            $("#help2").html("Click on the keyword to see help.");
            isHelpForWordLoaded = false;
        }
    }


    var counterForExamplesHelp = 0;
    var isHelpForExampleLoaded = false;

    function loadHelpForExample(name) {
        if (helpForExamples.length <= 0 && counterForExamplesHelp < 10) {
            setTimeout(function () {
                counterForExamplesHelp++;
                loadHelpForExample(name);
            }, 100);
        } else {
            counterForExamplesHelp = 0;
            isHelpForExampleLoaded = false;
            forEachInArrayWithArgs(helpForExamples, name, compareHelpForExamples);
            if (!isHelpForExampleLoaded) {
                $("#help1").html("Description not available.");
            }
        }

    }

    function compareHelpForWords(name, elementArray) {
        if (name == elementArray.name) {
            $("#help2").html(elementArray.text);
            isHelpForWordLoaded = true;
        }
    }

    function compareHelpForExamples(name, elementArray) {
        if (name == elementArray.name) {
            $("#help1").html(elementArray.text);
            isHelpForExampleLoaded = true;
        }
    }

    return HelpView;
})();