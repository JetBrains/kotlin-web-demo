/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
/*Messages*/
//Problems with ajax-request to server
var REQUEST_ABORTED = "Ajax request aborted.";
//Message in popup with warning before close tab with editor
var BEFORE_EXIT = "You have unsaved changes.";
var ERROR_UNTIL_EXECUTE = "During program execution errors have occurred.";
var TRY_RUN_CODE_WITH_ERROR = "See Problems View tab, there are errors in your code.";
var EXECUTE_OK = "Compilation competed without errors.";
var GET_FROM_APPLET_FAILED = "Applet doesn't supported on you computer.";
var LOADING_EXAMPLE_OK = "Example is loaded.";

var sessionId;
var isApplet = false;
var kotlinVersion;

var isContentEditorChanged = false;

function setMode(mode) {
    if (mode == "APPLET") {
        isApplet = true;
    }
}

function setSessionId(id) {
    var cookie = document.cookie;
    if (cookie != id) {
        document.cookie = id;
    }
    /*if (cookie == "") {
        document.cookie = id;
    } else {
        id = cookie;
    }*/
    sessionId = id;
    var data = "browser: " + navigator.appName + " " + navigator.appVersion;
    data += " " + "system: " + navigator.platform;
    $.ajax({
        url:document.location.href + "?sessionId=" + id + "&userData=true",
        context:document.body,
        type:"POST",
        data:{text:data},
        timeout:5000,
        error:function () {
            setStatusBarMessage(REQUEST_ABORTED);
        }
    });
}

function showContent() {
    $('#loader').hide();
    $(".applet-enable").click();
}

function setKotlinVersion(version) {
    kotlinVersion = version;
    document.getElementById("kotlinVersionTop").innerHTML = "(" + kotlinVersion + ")";
    document.getElementById("kotlinVersion").innerHTML = kotlinVersion;
}

function setStatusBarMessage(message) {
    document.getElementById("statusbar").innerHTML = message;
}

function setConsoleMessage(message) {
    document.getElementById("console").innerHTML = message;
}

/*var hashLoc = location.hash;
 window.addEventListener("unload", beforeUnload, true);


 function beforeUnload(e) {
 if (location.hash != hashLoc) {
 if (confirm(BEFORE_EXIT)) {
 history.back();
 }
 }
 }*/

/*document.unload = function () {
 if (confirm(BEFORE_EXIT)) {
 history.back();
 }
 };*/

function beforeBack() {
    if (isContentEditorChanged && confirm(BEFORE_EXIT)) {
        history.back();
    }
    isContentEditorChanged = false;
//    bajb_backdetect.OnBack = beforeBack();
}


//bajb_backdetect.OnBack = beforeBack;