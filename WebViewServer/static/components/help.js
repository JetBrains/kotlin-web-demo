/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/18/11
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
var isHelpForExampleLoaded = false;
var isHelpForWordLoaded = false;

function forEachInExamplesArray(arr, data, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(data, arr[i]);
        i++;
        if (isHelpForExampleLoaded) break;
    }
}

function forEachInWordsArray(arr, data, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(data, arr[i]);
        i++;
        if (isHelpForWordLoaded) break;
    }
}

function forEach(arr, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(arr[i]);
        i++;
    }
}

function updateHelp(str) {
    loadWordsHelp(str);
}

var helpForExamples = [];
var helpForWords = [];

function HelpElement(name, text, args, mode) {
    this.name = name;
    this.text = text;
    this.args = args;
    this.mode = setMode(mode);
    this.fullMode = mode;

    function setMode(fullmode) {
        var pos = fullmode.indexOf(" ");
        if (pos >= 0) {
            return fullmode.substring(0, pos);
        }
        return fullmode;
    }

}

var counterForExamplesHelp = 0;

function loadExampleHelp(name) {
    if (helpForExamples.length <= 0 && counterForExamplesHelp < 10) {
        setTimeout(function () {
            counterForExamplesHelp++;
            loadExampleHelp(name);
        }, 100);
    } else {
        counterForExamplesHelp = 0;
        isHelpForExampleLoaded = false;
        forEachInExamplesArray(helpForExamples, name, compareHelpForExamples);
        if (!isHelpForExampleLoaded) {
            document.getElementById("help1").innerHTML = "Description not available.";
            document.getElementById("arguments").value = "";
            $("#runConfigurationMode").selectmenu("value", "java");
        }
    }

}

function compareHelpForExamples(name, elementArray) {
    if (name == elementArray.name) {
        runConfiguration.mode = elementArray.mode;
        document.getElementById("help1").innerHTML = elementArray.text;
        document.getElementById("arguments").value = elementArray.args;
        $("#runConfigurationMode").selectmenu("value", elementArray.mode);

//        alert("help" + runConfiguration.mode);
        isHelpForExampleLoaded = true;
    }
}

var counter = 0;

function setRunConfForAllExamples() {
//    forEachInExamplesArray(helpForExamples, name, compareHelpForExamples);
    if (helpForExamples.length == 0 && counter < 10) {
        counter++;
        setTimeout(setRunConfForAllExamples, 100);
    } else {
        counter = 0;
        var i = 0;
        while (helpForExamples[i] != undefined) {
            var name = replaceAll(helpForExamples[i].name, " ", "_");
            if (helpForExamples[i].fullMode == "") {
                $("div[id='bullet" + name + "']").css("background", "url(/icons/java.png) no-repeat");
                $("div[id='bullet" + name + "']").attr("title", "java");
            } else {
                $("div[id='bullet" + name + "']").css("background", "url(/icons/" + replaceAll(helpForExamples[i].fullMode, " ", "") + ".png) no-repeat");
                if (helpForExamples[i].fullMode == "java js") {
                    $("div[id='bullet" + name + "']").attr("title", "Java / JavaScript");
                } else if (helpForExamples[i].fullMode == "java") {
                    $("div[id='bullet" + name + "']").attr("title", "Java (Standard)");
                } else if (helpForExamples[i].fullMode == "js") {
                    $("div[id='bullet" + name + "']").attr("title", "JavaScript (Standard)");
                } else if (helpForExamples[i].fullMode == "canvas") {
                    $("div[id='bullet" + name + "']").attr("title", "JavaScript (Canvas)");
                }
            }
            i++;
        }
    }

}

function loadHelpContentForExamples() {
    loadHelpContentForWords();
    $.ajax({
        url:generateAjaxUrl("loadHelpForExamples", "null"),
        context:document.body,
        success:onLoadingHelpForExamplesSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:30000,
        error:function () {
            setStatusBarMessage(HELP_REQUEST_ABORTED);
        }
    });
}

function onLoadingHelpForExamplesSuccess(data) {
    if (data != null) {
        var i = 0;
        while (typeof data[i] != "undefined") {
            var helpEl = new HelpElement(data[i].name, data[i].text, data[i].args, data[i].mode);
            helpForExamples.push(helpEl);
            i++;
        }
    }
}

function loadHelpContentForWords() {
    $.ajax({
        url:generateAjaxUrl("loadHelpForWords", "null"),
        context:document.body,
        success:onLoadingHelpForWordsSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:30000,
        error:function () {
            setStatusBarMessage(HELP_REQUEST_ABORTED);
        }
    });
}

function onLoadingHelpForWordsSuccess(data) {
    if (data != null) {
        var i = 0;
        while (typeof data[i] != "undefined") {
            var helpEl = new HelpElement(data[i].name, data[i].text, "", "java");
            helpForWords.push(helpEl);
            i++;
        }
    }
}

function loadWordsHelp(name) {
    isHelpForWordLoaded = false;
    forEachInWordsArray(helpForWords, name, compareHelpForWords)
    if (!isHelpForWordLoaded) {
        document.getElementById("help2").innerHTML = "Click on the keyword to see help.";
    }
}

function compareHelpForWords(name, elementArray) {
//    alert(name + elementArray.name);
    if (name == elementArray.name) {
        document.getElementById("help2").innerHTML = elementArray.text;
        isHelpForWordLoaded = true;
    }
}






