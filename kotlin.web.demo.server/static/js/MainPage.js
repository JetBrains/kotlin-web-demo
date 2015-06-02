/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

var sessionId = -1;

var app = Kotlin.modules["kotlin.web.demo.frontend"].application.app;

var configurationManager = new ConfigurationComponent();

var incompleteActionManager = new IncompleteActionManager();
incompleteActionManager.registerAction("save", "onHeadersLoaded",
    function () {
        localStorage.setItem("contentToSave", JSON.stringify(accordion.selectedProjectView.project));
    },
    function () {
        var content = JSON.parse(localStorage.getItem("contentToSave"));
        localStorage.removeItem("contentToSave");
        if (content != null && loginView.isLoggedIn) {
            openSaveProjectDialog(
                content.name,
                projectProvider.forkProject.bind(null, content, function (data) {
                    accordion.addNewProjectWithContent(data.publicId, JSON.parse(data.content));
                })
            );
        }
    });

var editor = new Kotlin.modules["kotlin.web.demo.frontend"].views.Editor(
    function (cursorPosition) {
        helpViewForWords.hide();
        var messageForLineAtCursor = editor.getMessageForLineAtCursor(cursorPosition);
        //Save previous message if current is empty
        if (messageForLineAtCursor != "") {
            statusBarView.setMessage(messageForLineAtCursor);
        }

        var pos = editor.cursorCoords();
        helpViewForWords.setPosition(pos);

        if (timer) {
            clearTimeout(timer);
            timer = setTimeout(function () {
                helpViewForWords.update(editor.getWordAtCursor(cursorPosition))
            }, 1000);
        } else {
            timer = setTimeout(function () {
                helpViewForWords.update(editor.getWordAtCursor(cursorPosition))
            }, 1000);
        }
    }
);

$(document).on("click", ".ui-widget-overlay", (function(){
    $(".ui-dialog-titlebar-close").trigger('click');
}));

var generatedCodeView = new Kotlin.modules["kotlin.web.demo.frontend"].views.
    GeneratedCodeView(document.getElementById("generated-code"));
$("#result-tabs").tabs();

var consoleView = new Kotlin.modules["kotlin.web.demo.frontend"].views.ConsoleView(document.getElementById("program-output"), $("#result-tabs"));
var junitView = new Kotlin.modules["kotlin.web.demo.frontend"].views.JUnitView(document.getElementById("program-output"), $("#result-tabs"));
var problemsView = new Kotlin.modules["kotlin.web.demo.frontend"].views.ProblemsView(
    document.getElementById("problems"),
    $("#result-tabs"),
    function (filename, line, ch) {
        accordion.selectedProjectView.getFileViewByName(filename).fireSelectEvent();
        editor.setCursor(line, ch);
        editor.focus();
    }
);


document.getElementById("shortcuts-button").onclick = function(){
    Kotlin.modules["kotlin.web.demo.frontend"].views.dialogs.ShortcutsDialogView.open()};

var helpModelForWords = new Kotlin.modules["kotlin.web.demo.frontend"].providers.HelpProvider();
var helpViewForWords = new Kotlin.modules["kotlin.web.demo.frontend"].views.HelpView(helpModelForWords);
helpViewForWords.hide();

var loginProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.LoginProvider(
    function () {
        if (accordion.selectedFileView != null) {
            accordion.selectedFileView.file.save();
        }
        accordion.selectedProjectView.project.save();
    },
    function () {
        getSessionInfo(function (data) {
            sessionId = data.id;
            loginView.logout();
            statusBarView.setStatus(ActionStatusMessages.logout_ok);
            accordion.loadAllContent();
        });
    },
    function (data) {
        if (data.isLoggedIn) {
            loginView.setUserName(data.userName, data.type);
            statusBarView.setStatus(ActionStatusMessages.login_ok);
        }
        accordion.loadAllContent();
    },
    function (exception, actionCode) {
        consoleView.writeException(exception);
        statusBarView.setMessage(actionCode);
    }
);

var loginView = new Kotlin.modules["kotlin.web.demo.frontend"].views.LoginView(loginProvider);

function getNumberOfErrorsAndWarnings(data) {
    var noOfErrorsAndWarnings = 0;
    for (var filename in data) {
        noOfErrorsAndWarnings += data[filename].length
    }
    return noOfErrorsAndWarnings
}


configurationManager.onChange = function (configuration) {
    accordion.selectedProjectView.project.confType = Configuration.getStringFromType(configuration.type);
    editor.removeHighlighting();
    problemsView.clear();
    editor.updateHighlighting()
};

configurationManager.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(ActionStatusMessages.change_configuration_fail);
};

var navBarView = new Kotlin.modules["kotlin.web.demo.frontend"].views.NavBarView(document.getElementById("grid-nav"));

var accordion = app.accordion;

var timer;
var runButton = app.runButtonElement

var projectProvider = app.projectProvider


function generateAjaxUrl(type, parameters) {
    var url = [location.protocol, '//', location.host, "/"].join('');
    url = url + "kotlinServer?sessionId=" + sessionId + "&type=" + type;
    for (var parameterName in parameters) {
        url += "&" + parameterName + "=" + parameters[parameterName];
    }
    return url;
}

function getSessionInfo(callback) {
    $.ajax({
        url: generateAjaxUrl("getSessionInfo"),
        context: document.body,
        type: "GET",
        timeout: 10000,
        dataType: "json",
        success: callback
    });
}

window.onfocus = function(){
    getSessionInfo(function(data){
        if(sessionId != data.id || data.isLoggedIn != loginView.isLoggedIn){
            location.reload();
        }
    })
};

function openSaveProjectDialog(defaultValue, callback) {
    Kotlin.modules["kotlin.web.demo.frontend"].views.dialogs.InputDialogView.open(
        "Save project",
        "Project name:",
        "Save",
        defaultValue,
        function(){
            accordion.validateNewProjectName()
        },
        callback
    )
}

$("#saveAsButton").click(function () {
    if (loginView.isLoggedIn) {
        openSaveProjectDialog(
            accordion.selectedProjectView.project.name,
            projectProvider.forkProject.bind(null, accordion.selectedProjectView.project, function (data) {
                accordion.selectedProjectView.project.loadOriginal();
                accordion.addNewProjectWithContent(data.publicId, JSON.parse(data.content));
            })
        );
    } else {
        incompleteActionManager.incomplete("save");
        loginView.openLoginDialog(function(){
            incompleteActionManager.cancel("save");
        });
    }
});

window.onbeforeunload = function () {
    accordion.onBeforeUnload();
    incompleteActionManager.onBeforeUnload();
    localStorage.setItem("openedItemId", accordion.selectedProjectView.project.publicId);

    if (accordion.selectedFileView != null) {
        accordion.selectedFileView.file.save();
    }
    accordion.selectedProjectView.project.save();

    localStorage.setItem("highlightOnTheFly", document.getElementById("on-the-fly-checkbox").checked);
    //return null;
};

$("#runMode").selectmenu({
    icons: {button: "selectmenu-arrow-icon"}
});

var argumentsInputElement = document.getElementById("arguments");
argumentsInputElement.oninput = function () {
    accordion.selectedProjectView.project.args = argumentsInputElement.value;
};

$("#on-the-fly-checkbox")
    .prop("checked", localStorage.getItem("highlightOnTheFly") == "true")
    .on("change", function () {
        var checkbox = document.getElementById("on-the-fly-checkbox");
        editor.highlightOnTheFly = checkbox.checked;
        editor.updateHighlighting();
    });
editor.highlightOnTheFly = document.getElementById("on-the-fly-checkbox").checked;

function setKotlinVersion() {
    $.ajax("http://kotlinlang.org/latest_release_version.txt", {
        type: "GET",
        timeout: 1000,
        success: function (kotlinVersion) {
            document.getElementById("kotlinlang-kotlin-version").innerHTML = "(" + kotlinVersion + ")";
        }
    });
    document.getElementById("webdemo-kotlin-version").innerHTML = KOTLIN_VERSION;
}

var blockTimer;
function blockContent() {
    clearTimeout(blockTimer);
    var overlay = document.getElementById("global-overlay");
    overlay.style.display = "block";
    overlay.style.visibility = "hidden";
    overlay.focus();
    blockTimer = setTimeout(function () {
        overlay.style.visibility = "initial";
    }, 250);
}

function unBlockContent() {
    clearTimeout(blockTimer);
    document.getElementById("global-overlay").style.display = "none";
}

getSessionInfo(function(data){
    sessionId = data.id;
});
setKotlinVersion();