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

var BEFORE_EXIT = "The changes you made to the program will be lost when you change an example. Do you want to leave the page?";

var KOTLIN_VERSION = "0.8.313";
var APPLET_VERSION = "22012013";
var WEB_DEMO_VERSION = "${web.demo.version}";

String.prototype.endsWith = function (a) {
    return-1 !== this.indexOf(a, this.length - a.length);
};

String.prototype.startsWith = function (a) {
    return 0 === this.indexOf(a);
};

function addKotlinExtension(filename) {
    return filename.endsWith(".kt") ? filename : filename + ".kt";
}

function removeKotlinExtension(filename) {
    return filename.endsWith(".kt") ? filename.substring(0, filename.length - ".kt".length) : filename;
}

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

function compareNumbers(a, b) {
    return a - b;
}

var tagsToReplace = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;'
};

function unEscapeString(str) {
    for (var tag in tagsToReplace) {
        str = str.replace(new RegExp(tagsToReplace[tag], "g"), tag);
    }
    return str;
}



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
    return Math.floor((Math.random() * 100) + 1);
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

function copy(obejct) {
    var cp;

    if (null == obejct || "object" != typeof obejct) return obejct;

    if (obejct instanceof Array) {
        cp = [];
        for (var i = 0, len = obejct.length; i < len; i++) {
            cp[i] = copy(obejct[i]);
        }
        return cp;
    }

    if (obejct instanceof Object) {
        cp = {};
        for (var attr in obejct) {
            if (obejct.hasOwnProperty(attr)) cp[attr] = copy(obejct[attr]);
        }
        return cp;
    }

    return cp;
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

function createExampleId(name, folder) {
    return "folder=" + replaceAll(folder, " ", "%20") + "&project=" + replaceAll(name, " ", "%20");
}

function createUserProjectUrl(id) {
    return "project_" + id;
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

window.setInterval = function (code, delay) {
    var retval = window.oldSetInterval(code, delay);
    window.intervalList.push(retval);
    return retval;
};
window.clearInterval = function (id) {
    var ind = window.intervalList.indexOf(id);
    if (ind >= 0) {
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
window.clearAllIntervals = function () {
    for (var i in window.intervalList) {
        window.oldClearInterval(window.intervalList[i]);
    }
    window.intervalList = new Array();
};


function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
