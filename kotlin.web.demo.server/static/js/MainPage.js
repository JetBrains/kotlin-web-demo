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

var ActionStatusMessages = {
    load_example_ok: StatusBarView.Messages.load_example_ok,
    load_example_fail: StatusBarView.Messages.load_example_fail,
    load_program_ok: StatusBarView.Messages.load_program_ok,
    load_program_fail: StatusBarView.Messages.load_program_fail,
    load_programs_fail: StatusBarView.Messages.load_programs_fail,
    load_examples_fail: StatusBarView.Messages.load_examples_fail,
    generate_link_ok: StatusBarView.Messages.generate_link_ok,
    generate_link_fail: StatusBarView.Messages.generate_link_fail,
    delete_program_ok: StatusBarView.Messages.delete_program_ok,
    delete_program_fail: StatusBarView.Messages.delete_program_fail,
    save_program_ok: StatusBarView.Messages.save_program_ok,
    login_ok: StatusBarView.Messages.login_ok,
    login_fail: StatusBarView.Messages.login_fail,
    save_program_fail: StatusBarView.Messages.save_program_fail
};

var sessionId = -1;

function Example() {
    var name;
    var args;
    var text;
    var runner;
    var dependencies;
    var defaultDependencies;
}

var configurationManager = new ConfigurationComponent();
var actionManager = new ActionManager();

actionManager.registerAction("org.jetbrains.web.demo.run",
    new Shortcut("Ctrl+F9", function (e) {
        return e.keyCode == 120 && e.ctrlKey;
    }), new Shortcut("Ctrl+R", function (e) {
        return e.keyCode == 82 && e.ctrlKey;
    }));
actionManager.registerAction("org.jetbrains.web.demo.reformat",
    new Shortcut("Ctrl+Alt+L", null), /*default*/
    new Shortcut("Cmd+Alt+L", null));
/*mac*/
actionManager.registerAction("org.jetbrains.web.demo.autocomplete",
    new Shortcut("Ctrl+Space", null));
actionManager.registerAction("org.jetbrains.web.demo.save",
    new Shortcut("Ctrl+S", function (e) {
        return e.keyCode == 83 && e.ctrlKey;
    }), new Shortcut("Cmd+S", function (e) {
        return e.keyCode == 83 && e.metaKey;
    }));


var editor = new KotlinEditor();

var argumentsView = $("#arguments");
var statusBarView = new StatusBarView($("#statusbar"));
var generatedCodeView = new GeneratedCodeView($("#generated-code"));
var consoleView = new ConsoleView($("#console"), $("#result-tabs"));
var problemsView = new ProblemsView($("#problems"), $("#result-tabs"));

var canvasPopup = new CanvasPopup($("#popupForCanvas"));

//var runButton = new Button($("#run-button"), actionManager.getShortcutByName("org.jetbrains.web.demo.run").getName());

var helpModelForExamples = new HelpModel("Examples");
var helpViewForExamples = new HelpView("Examples", $("#example-help-text"), helpModelForExamples);
var helpModelForWords = new HelpModel("Words");
var helpViewForWords = new HelpView("Words", $("#words-help-text"), helpModelForWords);
helpViewForWords.hide();


var runProvider = new RunProvider();

var loginProvider = new LoginProvider();
var loginView = new LoginView(loginProvider);
var converterProvider = new ConverterProvider();
var converterView = new ConverterView($("#java2kotlin"), converterProvider);

var accordion = new AccordionView($("#examples-list"));
var highlighting = new HighlightingFromServer();

editor.setHighlighterDecorator(highlighting);


ConfirmDialog.isEditorContentChanged = editor.isEditorContentChanged;
ConfirmDialog.isLoggedIn = loginView.isLoggedIn;
ConfirmDialog.saveProgram = accordion.saveProgram;

converterProvider.onConvert = function (text) {
    converterView.closeDialog();
    editor.refreshMode();
    editor.setText(text);
    editor.indentAll();
    statusBarView.setMessage(StatusBarView.Messages.convert_java_to_kotlin_ok);
};

converterProvider.onFail = function (exception) {
    converterView.closeDialog();
    consoleView.writeException(exception);
    statusBarView.setMessage(StatusBarView.Messages.convert_java_to_kotlin_fail);
};

configurationManager.onChange = function (configuration) {
    editor.setConfiguration(configuration);
    consoleView.setConfiguration(configuration);
    accordion.setConfiguration(configuration);
};

configurationManager.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(StatusBarView.Messages.change_configuration_fail);
};

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


    if(timer) {
        clearTimeout(timer);
        timer = setTimeout(function(){helpViewForWords.update(editor.getWordAtCursor(cursorPosition))}, 1000);
    } else {
        timer = setTimeout(function(){helpViewForWords.update(editor.getWordAtCursor(cursorPosition))}, 1000);
    }
};


var run_button = $("#run-button")
    .button()
    .click(function () {
        run_button.button("option", "disabled", true);
        var localConfiguration = configurationManager.getConfiguration();
        highlighting.getHighlighting(localConfiguration.type, editor.getProgramText(), function (highlightingResult) {
            editor.addMarkers(highlightingResult);
            if (!checkIfThereAreErrorsInHighlightingResult(highlightingResult)) {
                //Create canvas element before run it in browser
                if (localConfiguration.type == Configuration.type.CANVAS) {
                    canvasPopup.show();
                }
                runProvider.run(configurationManager.getConfiguration(), editor.getProgramText(), argumentsView.val());
            } else {
                run_button.button("option", "disabled", false);
            }
        });
    });


runProvider.onExecutionFinish = function (output) {
    run_button.button("option", "disabled", false);
    consoleView.setOutput(output);
    statusBarView.setMessage(StatusBarView.Messages.run_java_ok);
};

runProvider.onFail = function (error) {
    run_button.button("option", "disabled", false);
    consoleView.writeException(error);
    statusBarView.setMessage(StatusBarView.Messages.run_java_fail);
};

ProgramsView.isLoggedIn = loginView.isLoggedIn;
ProgramsModel.getEditorContent = editor.getProgramText;
ProgramsModel.getArguments = argumentsView.val;


accordion.onFail = function (exception, actionCode) {
    consoleView.writeException(exception);
    statusBarView.setMessage(actionCode);
};
accordion.onLoadCode = function (element, isProgram) {
    if (!isProgram) {
        helpViewForExamples.update(element.name);
        statusBarView.setMessage(StatusBarView.Messages.load_example_ok);
    } else {
        helpViewForExamples.hide();
        statusBarView.setMessage(StatusBarView.Messages.load_program_ok);
    }

    var text = element.text;

    editor.setText(text);
    argumentsView.val(element.args);
    configurationManager.updateConfiguration(getFirstConfiguration(element.confType));

};

accordion.onDeleteProgram = function () {
    statusBarView.setMessage(StatusBarView.Messages.delete_program_ok);
};

accordion.onSaveProgram = function () {
    editor.markAsUnchanged();
    statusBarView.setMessage(StatusBarView.Messages.save_program_ok);
};

loginProvider.onLogin = function (data) {
    loginView.setUserName(data);
    statusBarView.setMessage(StatusBarView.Messages.login_ok);
    accordion.loadAllContent();
};

loginProvider.onLogout = function () {
    $("#examples-list").accordion("destroy");
    loginView.logout();
    statusBarView.setMessage(StatusBarView.Messages.logout_ok);
    accordion.loadAllContent();

};

loginProvider.onFail = function (exception, actionCode) {
    consoleView.writeException(exception);
    statusBarView.setMessage(actionCode);
};

$(document).keydown(function (e) {
    var shortcut = actionManager.getShortcutByName("org.jetbrains.web.demo.run");
    if (shortcut.isPressed(e)) {
        run_button.click();
    } else {
        shortcut = actionManager.getShortcutByName("org.jetbrains.web.demo.save");
        if (shortcut.isPressed(e)) {
            accordion.saveProgram();
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

//function loadShortcuts() {
//    var text = $("#help3").html();
//    text = text.replace("@shortcut-autocomplete@", actionManager.getShortcutByName("org.jetbrains.web.demo.autocomplete").getName());
//    text = text.replace("@shortcut-run@", actionManager.getShortcutByName("org.jetbrains.web.demo.run").getName());
//    text = text.replace("@shortcut-reformat@", actionManager.getShortcutByName("org.jetbrains.web.demo.reformat").getName());
//    text = text.replace("@shortcut-save@", actionManager.getShortcutByName("org.jetbrains.web.demo.save").getName());
//    $("#help3").html(text);
//}

$("#whatimgjavatokotlin").click(function () {
    $("#dialogAboutJavaToKotlinConverter").dialog("open");
});

$("#dialogAboutJavaToKotlinConverter").dialog({
    modal: "true",
    width: 300,
    autoOpen: false
});

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

$("#popupForCanvas").dialog({
    width: 630,
    height: 350,
    autoOpen: false,
    close: function () {
        canvasPopup.hide();
        //$("#popupForCanvas").html("");
    }
});


$("#save").click(function () {
    if (ProgramsView.isLoggedIn()) {
        $("#save-dialog").dialog("open");
    } else {
        $("#login-dialog").dialog("open");
    }
})

run_button.attr("title", run_button.attr("title").replace("@shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.run").getName()));
$("#save").attr("title", $("#save").attr("title").replace("@shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.save").getName()));

function loadShortcuts() {
    var text = $("#shortcuts-help").html();
    text = text.replace("@completion_shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.autocomplete").getName());
    text = text.replace("@run_shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.run").getName());
    text = text.replace("@reformat_shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.reformat").getName());
    text = text.replace("@save_shortcut@", actionManager.getShortcutByName("org.jetbrains.web.demo.save").getName());
    $("#shortcuts-help").html(text);
}

helpDialog = $("#help-dialog");
helpDialog.dialog(
    {
        width: 350,
        autoOpen: false,
        modal: true
    }
);

$("#help").click(function () {
        helpDialog.dialog("open")
    }
);

$("#run-mode").selectmenu({
    icons:{button: "selectmenu-arrow-icon"}
});

var show = function () {
    document.getElementById("console-image").className = "console-image-active";
    document.getElementById("console-image").onclick = hide;
    document.getElementById("console-button").className = "console-arguments-button-active";
    document.getElementById("command-line-arguments").className = "command-line-arguments-visible";

    document.getElementById("scroll").style.height = "513px";
    document.getElementById("gutter").style.height = "513px";
};

var hide = function () {
    document.getElementById("console-image").className = "console-image";
    document.getElementById("console-image").onclick = show;
    document.getElementById("console-button").className = "console-arguments-button";
    document.getElementById("command-line-arguments").className = "command-line-arguments-hidden";

    document.getElementById("scroll").style.height = "548px";
    document.getElementById("gutter").style.height = "548px";
};
document.getElementById("console-image").onclick = hide;

document.getElementById("scroll").style.height = "513px";
document.getElementById("gutter").style.height = "513px";

setSessionId();
loadShortcuts();
