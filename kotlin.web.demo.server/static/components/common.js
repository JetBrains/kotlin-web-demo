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
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 2:05 PM
 */

var BEFORE_EXIT = "The changes you made to the program will be lost when you change an example. Do you want to leave the page?";

var KOTLIN_VERSION = "0.0.0";
var APPLET_VERSION = "14092012";
var WEB_DEMO_VERSION = "0.0.0";

function forEachInArrayWithArgs(arr, data, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(data, arr[i]);
        i++;
    }
}

function forEachInArray(arr, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(arr[i]);
        i++;
    }
}

function unEscapeString(str) {
    str = replaceAll(str, "&amp;", "&");
    return str;
}

var tagsToReplace = {
    '&':'&amp;',
    '<':'&lt;',
    '>':'&gt;'
};

function replaceTag(tag) {
    return tagsToReplace[tag] || tag;
}

function safe_tags_replace(str) {
    try {
        return str.replace(/[&<>]/g, replaceTag);
    } catch (e) {
        return str;
    }

}

function replaceAll(str, replaced, replacement) {
    try {
        return str.replace(new RegExp(replaced, 'g'), replacement)
    } catch (e) {
        return str;
    }
}

function getFirstConfiguration(confStr) {
    var pos = confStr.indexOf(" ");
    if (pos >= 0) {
        return confStr.substring(0, pos);
    }
    return confStr;
}

function checkDataForNull(data) {
    return !(data == null || data == undefined);

}

function checkDataForException(data) {
        //consoleView.writeException(data);
    return !(data[0] != null && data[0] != undefined && data[0].exception != undefined);

}

function random() {
    return Math.floor((Math.random()*100)+1);
}

function checkIfThereAreErrorsInHighlightingResult(highlightingResult) {
    var i = 0;
    while (highlightingResult[i] != undefined) {
        var severity = highlightingResult[i].severity;
        if (severity == "ERROR") {
            return true;
        }
        i++;
    }
    return false;
}

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


//Intervals: clear interval for canvas

//window.timeoutList = new Array();
window.intervalList = new Array();

//window.oldSetTimeout = window.setTimeout;
window.oldSetInterval = window.setInterval;
//window.oldClearTimeout = window.clearTimeout;
window.oldClearInterval = window.clearInterval;

/*window.setTimeout = function(code, delay) {
    var retval = window.oldSetTimeout(code, delay);
    window.timeoutList.push(retval);
    return retval;
};
window.clearTimeout = function(id) {
    var ind = window.timeoutList.indexOf(id);
    if(ind >= 0) {
        window.timeoutList.splice(ind, 1);
    }
    var retval = window.oldClearTimeout(id);
    return retval;
};*/

window.setInterval = function(code, delay) {
    var retval = window.oldSetInterval(code, delay);
    window.intervalList.push(retval);
    return retval;
};
window.clearInterval = function(id) {
    var ind = window.intervalList.indexOf(id);
    if(ind >= 0) {
        window.intervalList.splice(ind, 1);
    }
    var retval = window.oldClearInterval(id);
    return retval;
};
/*window.clearAllTimeouts = function() {
    for(var i in window.timeoutList) {
        window.oldClearTimeout(window.timeoutList[i]);
    }
    window.timeoutList = new Array();
};*/
window.clearAllIntervals = function() {
    for(var i in window.intervalList) {
        window.oldClearInterval(window.intervalList[i]);
    }
    window.intervalList = new Array();
};

