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
 * To change this template use File | Settings | File Templates.
 */
/*Messages*/
//Problems with ajax-request to server
var HIGHLIGHT_REQUEST_ABORTED = "Can't get errors/warnings.";
var RUN_REQUEST_ABORTED = "Can't get program output from server.";
var COMPLETE_REQUEST_ABORTED = "Can't get completion proposal list from server.";
var EXAMPLES_REQUEST_ABORTED = "Can't get the example code from server.";
var SAVE_PROGRAM_REQUEST_ABORTED = "Can't save the program on server.";
var LOAD_PROGRAM_REQUEST_ABORTED = "Can't load the program from server.";
var DELETE_PROGRAM_REQUEST_ABORTED = "Can't delete the program from server.";
var PUBLIC_LINK_REQUEST_ABORTED = "Can't geerate the public link for program.";
var HELP_REQUEST_ABORTED = "Can't get help from server.";
//Message in popup with warning before close tab with editor
//var BEFORE_EXIT = "The changes you made to the program will be lost when this page is closed. Do you want to close the page?";
var BEFORE_EXIT = "The changes you made to the program will be lost when you change an example. Do you want to leave the page?";
var BEFORE_DELETE_PROGRAM = "Do you really want to delete the program?";
var ERROR_UNTIL_EXECUTE = "Your program has terminated with an exception.";
var TRY_RUN_CODE_WITH_ERROR = "Can't run a program with errors. See the Problems View tab.";
var EXECUTE_OK = "Compilation competed successfully.";
var GET_FROM_APPLET_FAILED = "Your browser can't run Java Applets.";
var LOADING_HIGHLIGHTING_OK = "Errors were loaded.";
var LOADING_EXAMPLE_OK = "Example is loaded.";
var LOADING_PROGRAM_OK = "Program is loaded.";
var COMPILE_IN_JS_APPLET_ERROR = "The Pre-Alpha JavaScript back-end could not generate code for this program.<br/>Try to run it using JVM.";
var SHOW_JAVASCRIPT_CODE = "Show generated JavaScript code";
var COMPLETION_ISNOT_AVAILABLE = "Switch to \"Client\" or \"Server\" mode to enable completion";
var IE_SUPPORT = "Sorry, Internet Explorer is currently unsupported.";

var KOTLIN_VERSION = "0.0.0";
var WEB_DEMO_VERSION = "0.0.0";

var sessionId = -1;
var userName = "";
var isApplet = false;
var isJsApplet = true;

var runConfiguration = new RunConfiguration();

function setRunConfigurationMode() {
    var mode = $("#runConfigurationMode").val();
    if (mode == "" && counterSetConfMode < 10) {
        counterSetConfMode++;
        setTimeout(setRunConfigurationMode, 100);
    } else {
        counterSetConfMode = 0;
        runConfiguration.mode = mode;
    }
}

var isMac = false;

var isAppletLoaded = false;

//var isContentEditorChanged = false;

var isLogin = false;

var editorState = new EditorState(false);

function EditorState(isChanged) {
    this.isContentChanged = isChanged;
}

function setEditorState(state) {
    editorState = new EditorState(state);
}

function getEditorState() {
    return editorState.isContentChanged;
}



$(document).keydown(function (e) {
    if (isMac) {
        if (e.keyCode == 82 && e.ctrlKey && e.shiftKey) {
            runConfiguration.mode = "js";
            $("#runConfigurationMode").selectmenu("value", "js");
            $("#run").click();
        } else if (e.keyCode == 82 && e.ctrlKey) {
            $("#run").click();
        } else if (e.keyCode == 83 && e.metaKey) {
            stopKeydown(e);
            save();
        }
    } else {
        if (e.keyCode == 120 && e.ctrlKey && e.shiftKey) {
            runConfiguration.mode = "js";
            $("#runConfigurationMode").selectmenu("value", "js");
            $("#run").click();
        } else if (e.keyCode == 120 && e.ctrlKey) {
            $("#run").click();
        } else if (e.keyCode == 83 && e.ctrlKey) {
            stopKeydown(e);
            save();
        }
    }
});

function RunConfiguration() {
    this.mode = "";

}

function stopKeydown(e) {
    e_preventDefault(e);
}

function e_preventDefault(e) {
    if (e.preventDefault) e.preventDefault();
    else e.returnValue = false;
}


function onBodyLoad() {
    if (navigator.appVersion.indexOf("MSIE") != -1) {
        document.getElementsByTagName("body")[0].innerHTML = IE_SUPPORT;
    } else {
        $("#help3").toggle(true);
        resizeCentral();
        setKotlinVersion();
        setTimeout(function() {
            if (navigator.appVersion.indexOf("Mac") != -1) {
                isMac = true;
                var text = $("#help3").html();
                text = text.replace("F9", "R");
                $("#help3").html(text.replace("F9", "R"));
                var title = $("#run").attr("title").replace("F9", "R");
                $("#run").attr("title", title);
//            document.getElementById("run").title = document.getElementById("run").title.replace("F9", "R");
//            document.getElementById("runJS").title = document.getElementById("runJS").title.replace("F9", "R");
            }
        }, 10);

        hideLoader();
        setTimeout(setSessionId, 10);
//        setSessionId();
    }
}

function setSessionId() {
    var id;
    $.ajax({
        url:generateAjaxUrl("getSessionId", "null"),
        context:document.body,
        type:"GET",
        dataType:"json",
        timeout:10000,
        success:getSessionIdSuccess
    });

}

function logout() {
    $.ajax({
        url:generateAjaxUrl("authorization", "logout"),
        type:"GET",
        dataType:"json",
        timeout:5000,
        success:function () {
            $("#examplesaccordion").html("");
            loadAccordionContent();
            isLogin = false;
            $("#login").css("display", "block");
            $("#userName").html("");
            $("#userName").css("display", "none");
        }
    });

}

function getSessionIdSuccess(data) {
    data = eval(data);
    if (data[0] != null && data[0] != '') {
        sessionId = data[0];
    }
    if (data[1] != null && data[1] != '') {
        userName = data[1];
        if (userName != "") {
            $("#login").css("display", "none");
            $("#userName").css("display", "block");
            isLogin = true;
            userName = decodeURI(userName);
            userName = replaceAll(userName, "\\+", " ");

            $("#userName").html("<div id='userNameTitle'><span>Welcome, " + userName + "</span><img src='/static/images/toogleShortcutsOpen.png' id='userNameImg'/></div>");
            document.getElementById("userNameTitle").onclick = function (e) {
                userNameClick(e);
            };

            /* document.getElementById("userName").innerHTML = "<select id=\"userNameSelect\" ><option value='"+ userName + "'>" + userName + "</option><option value='Logout'>Logout</option></select>";
             $("#userNameSelect").selectmenu({
             width: 245,
             style:'popup',
             select: function() {
             if (this.value == "Logout") {
             logout();
             }
             }
             });*/
        }

    }

    loadHelpContentForExamples();
    loadAccordionContent();

    var info = "browser: " + navigator.appName + " " + navigator.appVersion;
    info += " " + "system: " + navigator.platform;

    var mode = getModeFromCookies();
    if (mode == "applet") {
        $(".applet-enable").click();
    } else if (mode == "server") {
        $(".applet-disable").click();
    } else {
        $(".applet-nohighlighting").click();
    }

    $.ajax({
        url:generateAjaxUrl("sendUserData", "null"),
        context:document.body,
        type:"POST",
        data:{text:info},
        timeout:5000
    });
}

function resizeCentral() {
    var wheight = (window.innerHeight) ? window.innerHeight :
        ((document.all) ? document.body.offsetHeight : null);
    var wwidth = (window.innerWidth) ? window.innerWidth :
        ((document.all) ? document.body.offsetWidth : null);
    $("#scroll").css("height", (wheight - 262 - 72 - 20 - 10) + "px");
    $("#left").css("minHeight", (wheight - 72 - 20 - 10) + "px");
    $("#right").css("minHeight", (wheight - 72 - 20 - 10) + "px");
    $("#center").css("minHeight", (wheight - 72 - 20 - 10) + "px");
    $("#center").css("width", (wwidth - 292 - 292 - 2 - 20 -5) + "px");
//    $("#left").css("width", "280px");
//    $("#right").css("width", "280px");
    editor.refresh();
}

function hideLoader() {
    $('#loader').hide();
}

function showLoader() {
//    document.getElementById("loader").style.display = 'block';
    $('#loader').show();
}

function setKotlinVersion() {
    $("#dialog").dialog({
        modal:"true",
        width:400,
        autoOpen:false
    });

    $("#kotlinVersionTop").html("(" + KOTLIN_VERSION + ")");
    $("#kotlinVersion").html(WEB_DEMO_VERSION);
}

function setStatusBarMessage(message) {
    $("#statusbar").html(message);
}
function setStatusBarError(message) {
    $("#statusbar").html("<font color=\"red\">" + message + "</font>");
}

function setConsoleMessage(message) {
    $("#console").html(message);
}

function clearProblemView() {
    $("#problems").html("");
}

function unEscapeString(str) {
    str = replaceAll(str, "&amp;", "&");
    return str;
}

var generatedJSCode = "";

function showJsCode() {
    document.getElementById("console").removeChild(document.getElementById("console").childNodes[1]);
    $("#console :first-child").after("<p class='consoleViewInfo'>" + generatedJSCode + "</p>");
}

$("#whatimg").click(function () {
    $("#dialog").dialog("open");
});

$("#whatimgRunConf").click(function () {
    $("#dialogAboutRunConfiguration").dialog("open");
});

function generateAjaxUrl(type, args) {
    var url = [location.protocol, '//', location.host, "/"].join('');
    return url + "kotlinServer?sessionId=" + sessionId + "&type=" + type + "&args=" + args;
}

function beforeLogin(param) {
    if (getEditorState()) {
        confirmAction(function (param) {
            return function () {
                login(param);
            }
        }(param));
    } else {
        login(param);
    }
}

function login(param) {
    $.ajax({
        url:generateAjaxUrl("authorization", param),
        context:document.body,
        success:onLoginSuccess,
        dataType:"text",
        type:"GET",
        //data:{text:i},
        timeout:10000,
        error:function () {

        }
    });
}

function onLoginSuccess(data) {
    document.location.href = data;
}

function showConfirmDialog(fun) {

    $("#confirmDialog").dialog({
        buttons:[
            { text:"Save changes",
                click:function () {
                    save();
                    closeConfirmDialog();
                }
            },
            { text:"Discard changes",
                click:function () {
                    closeConfirmDialog();
                    fun();
                }
            },
            { text:"Cancel",
                click:function () {
                    closeConfirmDialog();
                }
            }
        ]
    });

    $("#confirmDialog").dialog("open");
    if (!isLogin) {
        $(":button:contains('Save changes')").attr("disabled", "disabled").addClass("ui-state-disabled");
    }
}


function closeConfirmDialog() {
    $("#confirmDialog").dialog("close");
}

function confirmAction(fun) {
    showConfirmDialog(fun);
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

function setCookie(name, value, expires, path, domain, secure) {
    document.cookie = name + "=" + escape(value) +
        ((expires) ? "; expires=" + expires : "") +
        ((path) ? "; path=" + path : "") +
        ((domain) ? "; domain=" + domain : "") +
        ((secure) ? "; secure" : "");
}

function getCookie(name) {
    var cookie = " " + document.cookie;
    var search = " " + name + "=";
    var setStr = null;
    var offset = 0;
    var end = 0;
    if (cookie.length > 0) {
        offset = cookie.indexOf(search);
        if (offset != -1) {
            offset += search.length;
            end = cookie.indexOf(";", offset)
            if (end == -1) {
                end = cookie.length;
            }
            setStr = unescape(cookie.substring(offset, end));
        }
    }
    return(setStr);
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
    return str.replace(/[&<>]/g, replaceTag);
}

function replaceAll(str, replaced, replacement) {
    return str.replace(new RegExp(replaced, 'g'), replacement)
}

//FOR TESTS

function setLogin() {
    setEditorState(false);
    userName = "Natalia Ukhorskaya";
    $("#login").css("display", "none");
    $("#userName").css("display", "block");
    isLogin = true;
    userName = decodeURI(userName);
    userName = replaceAll(userName, "\\+", " ");

    $("#userName").html("<div id='userNameTitle'><span>Welcome, " + userName + "</span><img src='/static/images/toogleShortcutsOpen.png' id='userNameImg'/></div>");
    document.getElementById("userNameTitle").onclick = function (e) {
        userNameClick(e);
    };

    $("#examplesaccordion").html("");

    loadHelpContentForExamples();
    loadAccordionContent();
}









