/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
/*Messages*/
//Problems with ajax-request to server
var HIGHLIGHT_REQUEST_ABORTED = "Can't get errors/warnings from server.";
var RUN_REQUEST_ABORTED = "Can't get program output from server.";
var COMPLETE_REQUEST_ABORTED = "Can't get completion proposal list from server.";
var EXAMPLES_REQUEST_ABORTED = "Can't get the example code from server.";
var SAVE_PROGRAM_REQUEST_ABORTED = "Can't save the program on server.";
var DELETE_PROGRAM_REQUEST_ABORTED = "Can't delete the program from server.";
var HELP_REQUEST_ABORTED = "Can't get help from server.";
//Message in popup with warning before close tab with editor
//var BEFORE_EXIT = "The changes you made to the program will be lost when this page is closed. Do you want to close the page?";
var BEFORE_EXIT = "The changes you made to the program will be lost when you change an example. Do you want to leave the page?";
var BEFORE_DELETE_PROGRAM = "Do you really want to delete a program?";
var ERROR_UNTIL_EXECUTE = "Your program has terminated with an exception.";
var TRY_RUN_CODE_WITH_ERROR = "Can't run a program with errors. See the Problems View tab.";
var EXECUTE_OK = "Compilation competed successfully.";
var GET_FROM_APPLET_FAILED = "Your system can't run Java Applets.";
var LOADING_EXAMPLE_OK = "Example is loaded.";
var LOADING_PROGRAM_OK = "Program is loaded.";
var COMPILE_IN_JS_APPLET_ERROR = "The Pre-Alpha JavaScript back-end could not generate code for this program.<br/>Try to run it using JVM.";
var SHOW_JAVASCRIPT_CODE = "Show generated JavaScript code";
var COMPLETION_ISNOT_AVAILABLE = "Switch to \"Client\" or \"Server\" mode to enable completion";
var IE_SUPPORT = "Sorry, Internet Explorer is currently unsupported.";

var sessionId = -1;
var userName = "";
var isApplet = false;
var isJsApplet = true;
var kotlinVersion;

var isMac = false;

var isAppletLoaded = false;

var isContentEditorChanged = false;

var isLogin = false;

var loginImages = "";
//var loginImages = "<a href=\"#\" onclick=\"login('twitter');\"><img src=\"/images/social/twitter.png\"></a><a href=\"#\" onclick=\"login('facebook');\"><img src=\"/images/social/facebook.png\"></a><a href=\"#\" onclick=\"login('google');\"><img src=\"/images/social/google.png\"></a>";

$(document).keydown(function (e) {
    if (isMac) {
        if (e.keyCode == 82 && e.ctrlKey && e.shiftKey) {
            $("#runJS").click();
        } else if (e.keyCode == 82 && e.ctrlKey) {
            $("#run").click();
        }
    } else {
        if (e.keyCode == 120 && e.ctrlKey && e.shiftKey) {
            $("#runJS").click();
        } else if (e.keyCode == 120 && e.ctrlKey) {
            $("#run").click();
        }
    }
});


function onBodyLoad() {
    if (navigator.appVersion.indexOf("MSIE") != -1) {
        document.getElementsByTagName("body")[0].innerHTML = IE_SUPPORT;
    } else {
        if (navigator.appVersion.indexOf("Mac") != -1) {
            isMac = true;
            var text = document.getElementById("help3").innerHTML;
            text = text.replace("F9", "R");
            document.getElementById("help3").innerHTML = text.replace("F9", "R");
            document.getElementById("run").title = document.getElementById("run").title.replace("F9", "R");
            document.getElementById("runJS").title = document.getElementById("runJS").title.replace("F9", "R");
        }

        $("#help3").toggle(true);
        setSessionId();
        resizeCentral();
        setKotlinVersion('0.1.296');
        loadAccordionContent();
        loadHelpContentForExamples();
        hideLoader();
    }
}

function setSessionId() {
    var id;
    $.ajax({
        url:generateAjaxUrl("getSessionId", "null"),
        context:document.body,
        type:"GET",
        dataType:"json",
        timeout:5000,
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
            document.getElementById("examplesaccordion").innerHTML = "";
            loadAccordionContent();
            isLogin = false;
            document.getElementById("login").style.display = "block";
            document.getElementById("userName").innerHTML = "";
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
        /*if (loginImages == "") {
            loginImages = document.getElementById("userName").innerHTML;
        }*/
        if (userName != "") {
            document.getElementById("login").style.display = "none";
            isLogin = true;
            userName = decodeURI(userName);
            userName = userName.replace(new RegExp('\\+', 'g'), ' ');

//            document.getElementById("userName").innerHTML = "<b>" + userName + "</b>";
            document.getElementById("userName").innerHTML = "<select id=\"userNameSelect\" ><option value='"+ userName + "'>" + userName + "</option><option value='Logout'>Logout</option></select>";
            $("#userNameSelect").selectmenu({
                width: 245,
                style:'popup',
                select: function() {
                    if (this.value == "Logout") {
                        logout();
                    }
                }
            });
        }
    }
    var info = "browser: " + navigator.appName + " " + navigator.appVersion;
    info += " " + "system: " + navigator.platform;
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
    document.getElementById("scroll").style.height = (wheight - 262 - 72 - 20 - 10) + "px";
    document.getElementById("left").style.minHeight = (wheight - 72 - 20 - 10) + "px";
    document.getElementById("right").style.minHeight = (wheight - 72 - 20 - 10) + "px";
    document.getElementById("center").style.minHeight = (wheight - 72 - 20 - 10) + "px";
    editor.refresh();
}

function hideLoader() {
    $('#loader').hide();
}

function showLoader() {
//    document.getElementById("loader").style.display = 'block';
    $('#loader').show();
}

function setKotlinVersion(version) {
    $(".applet-disable").click();
    $(".applet-nohighlighting").click();
    $("#dialog").dialog({
        modal:"true",
        width:400,
        autoOpen:false
    });

    kotlinVersion = version;
    document.getElementById("kotlinVersionTop").innerHTML = "(" + kotlinVersion + ")";
    document.getElementById("kotlinVersion").innerHTML = kotlinVersion;
}

function setStatusBarMessage(message) {
    document.getElementById("statusbar").innerHTML = message;
}
function setStatusBarError(message) {
    document.getElementById("statusbar").innerHTML = "<font color=\"red\">" + message + "</font>";
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

function unEscapeString(str) {
    str = str.replace(new RegExp("&amp;", 'g'), "&");
    return str;

}

var generatedJSCode = "";

function showJsCode() {
    document.getElementById("console").removeChild(document.getElementById("console").childNodes[1]);
    var consoleStr = document.getElementById("console").innerHTML;
    consoleStr += "<p class='consoleViewInfo'>" + generatedJSCode + "</p>";
    setConsoleMessage(consoleStr);
}

$("#whatimg").click(function () {
    $("#dialog").dialog("open");
});

function generateAjaxUrl(type, args) {
    var url = [location.protocol, '//', location.host, "/"].join('');
    return url + "kotlinServer?sessionId=" + sessionId + "&type=" + type + "&args=" + args;
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
    <!-- Codes by Quackit.com -->
    // Popup window code
//        window.open(
//            data,'popUpWindow','height=700,width=800,left=10,top=10,resizable=yes,scrollbars=yes,toolbar=yes,menubar=no,location=no,directories=no,status=yes');

//    window.open(data);
    document.location.href = data;
}





