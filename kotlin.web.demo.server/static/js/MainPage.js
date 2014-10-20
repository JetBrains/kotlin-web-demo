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

var sessionId = -1;

var configurationManager = new ConfigurationComponent();
var actionManager = new ActionManager();

actionManager.registerAction("org.jetbrains.web.demo.run",
    new Shortcut(["Ctrl", "F9"], function (e) {
        return e.keyCode == 120 && e.ctrlKey;
    }), new Shortcut(["Ctrl", "R"], function (e) {
        return e.keyCode == 82 && e.ctrlKey;
    }));
actionManager.registerAction("org.jetbrains.web.demo.reformat",
    new Shortcut(["Ctrl", "Alt", "L"], null), /*default*/
    new Shortcut(["Ctrl", "Alt", "L"], null));
/*mac*/
actionManager.registerAction("org.jetbrains.web.demo.autocomplete",
    new Shortcut(["Ctrl", "Space"], null));
actionManager.registerAction("org.jetbrains.web.demo.save",
    new Shortcut(["Ctrl", "S"], function (e) {
        return e.keyCode == 83 && e.ctrlKey;
    }), new Shortcut(["Cmd", "S"], function (e) {
        return e.keyCode == 83 && e.metaKey;
    }));

var editor = new KotlinEditor();
var statusBarView = new StatusBarView(document.getElementById("status-bar"));
var generatedCodeView = new GeneratedCodeView($("#generated-code"));
var consoleView = new ConsoleView($("#console"), $("#result-tabs"));
var junitView = new JUnitView($("#console"), $("#result-tabs"));
var problemsView = new ProblemsView($("#problems"), $("#result-tabs"));

var canvas;
canvasDialog = $("#popupForCanvas").dialog({
    width: 630,
    height: 350,
    autoOpen: false,
    modal: true,
    close: function () {
        canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height);
        //element.dialog("close");
        //WARN: if in the page there is more intevals - they will be stopped.
        window.clearAllIntervals();
    }
});

canvas = document.getElementById("mycanvas");
canvas.setAttribute("width", canvasDialog.dialog("option", "width") + "");
canvas.setAttribute("height", (canvasDialog.dialog("option", "height") - 30) + "");

//var runButton = new Button($("#run-button"), actionManager.getShortcutByName("org.jetbrains.web.demo.run").getName());

var helpDialogView = new HelpDialogView();
var helpModelForWords = new HelpModel("Words");
var helpViewForWords = new HelpView("Words", $("#words-help-text"), helpModelForWords);
helpViewForWords.hide();


var runProvider = (function () {

    function onSuccess(output) {
        run_button.button("option", "disabled", false);
        if (configurationManager.getConfiguration().type == Configuration.type.JUNIT) {
            junitView.setOutput(output);
        } else {
            consoleView.setOutput(output);
        }
        statusBarView.setStatus(statusBarView.statusMessages.run_java_ok);
    }

    function onFail(error) {
        run_button.button("option", "disabled", false);
        consoleView.writeException(error);
        statusBarView.setStatus(statusBarView.statusMessages.run_java_fail);
    }

    return new RunProvider(onSuccess, onFail);
})();

var loginProvider = new LoginProvider();
var loginView = new LoginView(loginProvider);

var converterProvider = (function () {
    function onSuccess(data) {
        converterView.closeDialog();
        editor.refreshMode();
        editor.setText(data);
        editor.indentAll();
        statusBarView.setStatus(statusBarView.statusMessages.convert_java_to_kotlin_ok);
    }

    function onFail(error) {
        converterView.closeDialog();
        consoleView.writeException(error);
        statusBarView.setStatus(statusBarView.statusMessages.convert_java_to_kotlin_fail);
    }

    return new ConverterProvider(onSuccess, onFail);
})();

var highlightingProvider = (function () {
    function onSuccess(data, callback) {
        accordion.getSelectedProject().processHighlightingResult(data);
        problemsView.addMessages(data);

        var noOfErrorsAndWarnings = 0;

        for (var filename in data) {
            noOfErrorsAndWarnings += data[filename].length
        }
        statusBarView.setStatus(statusBarView.statusMessages.get_highlighting_ok, [noOfErrorsAndWarnings]);
        callback(data);
    }

    function onFail(error) {
        run_button.button("option", "disabled", false);
        consoleView.writeException(error);
        statusBarView.setStatus(statusBarView.statusMessages.get_highlighting_fail);
    }

    return new HighlichtingProvider(onSuccess, onFail)
})();

var completionProvider = (function () {
    function onSuccess(completionObject) {
        editor.showCompletionResult(completionObject);
        statusBarView.setStatus(statusBarView.statusMessages.get_completion_ok);
    }

    function onFail(error) {
        consoleView.writeException(error);
        statusBarView.setStatus(statusBarView.statusMessages.get_completion_fail);
    }

    return new CompletionProvider(onSuccess, onFail);
})();


configurationManager.onChange = function (configuration) {
    editor.setConfiguration(configuration);
    consoleView.setConfiguration(configuration);
    accordion.getSelectedProject().getProjectData().setConfiguration(Configuration.getStringFromType(configuration.type));
};

configurationManager.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(ActionStatusMessages.change_configuration_fail);
};

var converterView = new ConverterView($("#java2kotlin"), converterProvider);
var argumentsInput = document.getElementById("arguments");
argumentsInput.oninput = function () {
    accordion.getSelectedProject().getProjectData().setArguments(argumentsInput.value);
};

var accordion = (function () {
    var accordion = new AccordionView(document.getElementById("examples-list"));

    accordion.onProjectSelected = function (projectView) {
        argumentsInput.value = projectView.getProjectData().args;
        configurationManager.updateConfiguration(projectView.getProjectData().confType);
        helpDialogView.updateProjectHelp(projectView.getProjectData().help);
    };

    accordion.onFail = function (exception, actionCode) {
        consoleView.writeException(exception);
        statusBarView.setMessage(actionCode);
    };

    accordion.onDeleteProgram = function () {
        statusBarView.setStatus(statusBarView.statusMessages.delete_program_ok);
    };

    accordion.onSaveProgram = function () {
        editor.markAsUnchanged();
        statusBarView.setStatus(statusBarView.statusMessages.save_program_ok);
    };

    return accordion
})();

var timer;
editor.onCursorActivity = function (cursorPosition) {
    helpViewForWords.hide();
    var messageForLineAtCursor = editor.getMessageForLineAtCursor(cursorPosition);
    //Save previous message if current is empty
    if (messageForLineAtCursor != "") {
        statusBarView.setMessage(messageForLineAtCursor);
    }

    var wordsHelp = $("#words-help");
    var pos = editor.cursorCoords();
    wordsHelp.css("position", "absolute");
    wordsHelp.css("left", pos.x + "px");
    wordsHelp.css("top", pos.yBot + "px");


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
};


var run_button = $("#run-button")
    .button()
    .click(function () {
        run_button.button("option", "disabled", true);
        var localConfiguration = configurationManager.getConfiguration();
        highlightingProvider.getHighlighting(localConfiguration.type, accordion.getSelectedProject().getModifiableContent(), function (highlightingResult) {
            var example = accordion.getSelectedProject();
            example.processHighlightingResult(highlightingResult);
            if (!example.getProjectData().hasErrors()) {
                //Create canvas element before run it in browser
                if (localConfiguration.type == Configuration.type.CANVAS) {
                    canvasDialog.dialog("open");
                }
                runProvider.run(configurationManager.getConfiguration(), accordion.getSelectedProject().getModifiableContent(), accordion.getSelectedProject());
            } else {
                run_button.button("option", "disabled", false);
            }
        });
    });
//ProgramsModel.getEditorContent = editor.getProgramText;
//ProgramsModel.getArguments = argumentsView.val;




loginProvider.onLogin = function (data) {
    loginView.setUserName(data);
    statusBarView.setStatus(statusBarView.statusMessages.login_ok);
    accordion.loadAllContent();
};

loginProvider.onLogout = function () {
    accordion.getSelectedProject().save();
    loginView.logout();
    statusBarView.setStatus(statusBarView.statusMessages.logout_ok);
    accordion.loadAllContent();
};

loginProvider.onFail = function (exception, actionCode) {
    consoleView.writeException(exception);
    statusBarView.setMessage(actionCode);
};

var fileProvider = (function () {
    var fileProvider = new FileProvider();

    fileProvider.onFileRenamed = function () {
    };

    fileProvider.onRenameFileFailed = function () {
    };

    return fileProvider;
})();

var projectProvider = (function () {
    var projectProvider = new ProjectProvider();

    projectProvider.onProjectLoaded = function (projectContent) {
    };

    projectProvider.onFail = function () {
    };

    projectProvider.onNewProjectAdded = function (name, projectId, fileId) {
        accordion.addNewProject(name, projectId, fileId, null);
    };

    return projectProvider;
})();

var headersProvider = (function () {
    var headersProvider = new HeadersProvider();

    return headersProvider;
})();

$(document).keydown(function (e) {
    var shortcut = actionManager.getShortcutByName("org.jetbrains.web.demo.run");
    if (shortcut.isPressed(e)) {
        run_button.click();
    } else {
        shortcut = actionManager.getShortcutByName("org.jetbrains.web.demo.save");
        if (shortcut.isPressed(e)) {
            saveButton.click();
        }
    }

});

var isShortcutsShow = true;
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


function generateAjaxUrl(type, args) {
    var url = [location.protocol, '//', location.host, "/"].join('');
    return url + "kotlinServer?sessionId=" + sessionId + "&type=" + type + "&args=" + args;
}

function setSessionId() {
    $.ajax({
        url: generateAjaxUrl("getSessionId", "null"),
        context: document.body,
        type: "GET",
        dataType: "json",
        timeout: 10000,
        success: getSessionIdSuccess
    });
}

function getSessionIdSuccess(data) {
    data = eval(data);
    if (data[0] != null && data[0] != '') {
        sessionId = data[0];
    }

    var info = "browser: " + navigator.appName + " " + navigator.appVersion;
    info += " " + "system: " + navigator.platform;

    $.ajax({
        url: generateAjaxUrl("sendUserData", "null"),
        context: document.body,
        type: "POST",
        data: {text: info},
        timeout: 5000
    });
}


var saveButton = $("#save").click(function () {
    if (accordion.getSelectedProject().getType() == ProjectType.USER_PROJECT) {
        accordion.getSelectedProject().save();
    } else {
        accordion.getSelectedProject().saveAs();
    }
});

run_button.attr("title", run_button.attr("title").replace("@shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.run").getName()));
saveButton.attr("title", saveButton.attr("title").replace("@shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.save").getName()));

function loadShortcuts() {
    helpDialogView.addShortcut(actionManager.getShortcutByName("org.jetbrains.web.demo.autocomplete").getKeyNames(), "Code completion");
    helpDialogView.addShortcut(actionManager.getShortcutByName("org.jetbrains.web.demo.run").getKeyNames(), "Run program");
    helpDialogView.addShortcut(actionManager.getShortcutByName("org.jetbrains.web.demo.reformat").getKeyNames(), "Reformat selected fragment");
    helpDialogView.addShortcut(actionManager.getShortcutByName("org.jetbrains.web.demo.save").getKeyNames(), "Save current project");
}

window.onbeforeunload = closingCode;
function closingCode() {
    accordion.onBeforeUnload();
    localStorage.setItem("openedItemId", accordion.getSelectedProject().getPublicId());
    accordion.getSelectedProject().save();
    editor.save();
    return null;
}


var loginDialog = $("#login-dialog").dialog({
    modal: "true",
    width: 300,
    autoOpen: false
});

document.getElementById("help").onclick = helpDialogView.open;

$("#run-mode").selectmenu({
    icons: {button: "selectmenu-arrow-icon"}
});

var show = function () {
    document.getElementById("console-image").className = "console-image-active";
    document.getElementById("console-image").onclick = hide;
    document.getElementById("console-button").className = "console-arguments-button-active";
    document.getElementById("command-line-arguments").className = "command-line-arguments-visible";

    editor.resize();
};

var hide = function () {
    document.getElementById("console-image").className = "console-image";
    document.getElementById("console-image").onclick = show;
    document.getElementById("console-button").className = "console-arguments-button";
    document.getElementById("command-line-arguments").className = "command-line-arguments-hidden";

    editor.resize();
};
document.getElementById("console-image").onclick = hide;

editor.resize();

setKotlinVersion = function () {
    $("#kotlinVersionTop").html("(" + KOTLIN_VERSION + ")");
    $("#kotlinVersion").html(WEB_DEMO_VERSION);
    $("#currentYear").html(new Date().getFullYear());
};

//
//function fullScreenView(){
//    document.getElementById("global-header").style.display = "none";
//    document.getElementById("global-footer").style.display = "none";
//    $(".global-layout").addClass("fullscreen").addClass("no-after");
//    $(".g-layout").addClass("fullscreen");
//    $(".g-grid").addClass("fullscreen");
//}
//
//document.getElementById("fullscreen").onclick = fullScreenView;

function setKotlinJsOutput() {
    Kotlin.out = new Kotlin.BufferedOutput();
}

setSessionId();
loadShortcuts();
setKotlinJsOutput();
