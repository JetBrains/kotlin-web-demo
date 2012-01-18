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

function updateHelp(str) {
    loadWordsHelp(str);
}

var helpForExamples = [];
var helpForWords = [];

function HelpElement(name, text, args) {
    this.name = name;
    this.text = text;
    this.args = args;
}

function loadExamplesHelp(name) {
    isHelpForExampleLoaded = false;
    forEachInExamplesArray(helpForExamples, name, compareHelpForExamples)
    if (!isHelpForExampleLoaded) {
        document.getElementById("help1").innerHTML = "Description not available.";
        document.getElementById("arguments").value = "";
    }
}

function compareHelpForExamples(name, elementArray) {
    if (name == elementArray.name) {
        document.getElementById("help1").innerHTML = elementArray.text;
        document.getElementById("arguments").value = elementArray.args;
        isHelpForExampleLoaded = true;
    }
}

function loadHelpContentForExamples() {
    loadHelpContentForWords();
    $.ajax({
        url:document.location.href + generateAjaxUrl("loadHelpForExamples", "null"),
        context:document.body,
        success:onLoadingHelpForExamplesSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:30000,
        error:function () {
            setStatusBarMessage(REQUEST_ABORTED);
        }
    });
}

function onLoadingHelpForExamplesSuccess(data) {
    if (data != null) {
        var i = 0;
        while (typeof data[i] != "undefined") {
            var helpEl = new HelpElement(data[i].name, data[i].text, data[i].args);
            helpForExamples.push(helpEl);
            i++;
        }
    }
}

function loadHelpContentForWords() {
    $.ajax({
        url:document.location.href + generateAjaxUrl("loadHelpForWords", "null"),
        context:document.body,
        success:onLoadingHelpForWordsSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:30000,
        error:function () {
            setStatusBarMessage(REQUEST_ABORTED);
        }
    });
}

function onLoadingHelpForWordsSuccess(data) {
    if (data != null) {
        var i = 0;
        while (typeof data[i] != "undefined") {
            var helpEl = new HelpElement(data[i].name, data[i].text, "");
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






