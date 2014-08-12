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
    load_example_ok: "Example is loaded.",
    load_example_fail: "Can't load the example from server.",
    load_help_for_examples_ok: "Help for examples was loaded from server.",
    load_help_for_examples_fail: "Can't get help for examples from server.",
    load_help_for_words_ok: "Help for words was loaded from server.",
    load_help_for_words_fail: "Can't get help for words from server.",
    change_configuration_ok: "Configuration was changed.",
    change_configuration_fail: "Can't change configuration.",
    get_highlighting_ok: "Errors and warnings were loaded.",
    get_highlighting_fail: "Can't get errors/warnings.",
    get_completion_ok: "Completion proposal list was loaded from server.",
    get_completion_fail: "Can't get completion proposal list from server.",
    run_java_ok: "Compilation competed successfully.",
    run_java_fail: "Can't get program output.",
    run_js_ok: "Translation competed successfully.",
    run_js_fail: "Can't get translation result from server.",
    login_ok: "Log in successful.",
    login_fail: "Log in fail.",
    logout_ok: "Log out successful.",
    logout_fail: "Log out fail.",
    load_program_ok: "Program is loaded.",
    load_program_fail: "Can't load the program from server.",
    load_programs_fail: "Can't load programs from server.",
    load_examples_fail: "Can't load examples from server.",
    generate_link_ok: "Public link is generated.",
    generate_link_fail: "Can't generate the public link for program.",
    delete_program_ok: "Program is deleted.",
    delete_program_fail: "Can't delete the program from server.",
    convert_java_to_kotlin_ok: "Translation result was loaded in editor.",
    convert_java_to_kotlin_fail: "Can't convert you Java file to Kotlin.",
    save_program_ok: "Program was successfully saved.",
    save_program_fail: "Can't save the program on server.",
    loading_highlighting: "Loading highlighting...",
    loading_completion: "Loading completion..",


    get_result_from_applet_fail: "Your browser can't run Java Applets."
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
var statusBarView = $("#statusbar");
var generatedCodeView = new GeneratedCodeView($("#generated-code"));
var consoleView = new ConsoleView($("#console"), $("#result-tabs"));
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

var helpModelForExamples = new HelpModel("Examples");
var helpViewForExamples = new ExamplesHelpView($("#example-help-text"), helpModelForExamples);
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
    statusBarView.html(ActionStatusMessages.convert_java_to_kotlin_ok);
};

converterProvider.onFail = function (exception) {
    converterView.closeDialog();
    consoleView.writeException(exception);
    statusBarView.html(ActionStatusMessages.convert_java_to_kotlin_fail);
};

configurationManager.onChange = function (configuration) {
    editor.setConfiguration(configuration);
    consoleView.setConfiguration(configuration);
    accordion.setConfiguration(configuration);
};

configurationManager.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.html(ActionStatusMessages.change_configuration_fail);
};

var timer;
editor.onCursorActivity = function (cursorPosition) {
    helpViewForWords.hide();
    var messageForLineAtCursor = editor.getMessageForLineAtCursor(cursorPosition);
    //Save previous message if current is empty
    if (messageForLineAtCursor != "") {
        statusBarView.html(messageForLineAtCursor);
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
        highlighting.getHighlighting(localConfiguration.type, editor.getProgramText(), function (highlightingResult) {
            editor.addMarkers(highlightingResult);
            if (!checkIfThereAreErrorsInHighlightingResult(highlightingResult)) {
                //Create canvas element before run it in browser
                if (localConfiguration.type == Configuration.type.CANVAS) {
                    canvasDialog.dialog("open");
                }
                runProvider.run(configurationManager.getConfiguration(), editor.getProgramText(), argumentsView.val(), accordion.getSelectedExample());
            } else {
                run_button.button("option", "disabled", false);
            }
        });
    });


runProvider.onExecutionFinish = function (output) {
    run_button.button("option", "disabled", false);
    consoleView.setOutput(output);
    statusBarView.html(ActionStatusMessages.run_java_ok);
};

runProvider.onFail = function (error) {
    run_button.button("option", "disabled", false);
    consoleView.writeException(error);
    statusBarView.html(ActionStatusMessages.run_java_fail);
};

ProgramsView.isLoggedIn = loginView.isLoggedIn;
ProgramsModel.getEditorContent = editor.getProgramText;
ProgramsModel.getArguments = argumentsView.val;


accordion.onFail = function (exception, actionCode) {
    consoleView.writeException(exception);
    statusBarView.html(actionCode);
};
accordion.onLoadCode = function (element, isProgram) {
    if (!isProgram) {
        helpViewForExamples.showHelp(element.help);
        statusBarView.html(ActionStatusMessages.load_example_ok);
    } else {
        helpViewForExamples.hide();
        statusBarView.html(ActionStatusMessages.load_program_ok);
    }

    var text = element.files[0].content;

    editor.setText(text);
    argumentsView.val(element.args);
    configurationManager.updateConfiguration(getFirstConfiguration(element.confType));

};

accordion.onDeleteProgram = function () {
    statusBarView.html(ActionStatusMessages.delete_program_ok);
};

accordion.onSaveProgram = function () {
    editor.markAsUnchanged();
    statusBarView.html(ActionStatusMessages.save_program_ok);
};

loginProvider.onLogin = function (data) {
    loginView.setUserName(data);
        statusBarView.html(ActionStatusMessages.login_ok);
    accordion.loadAllContent();
};

loginProvider.onLogout = function () {
    $("#examples-list").accordion("destroy");
    loginView.logout();
    statusBarView.html(ActionStatusMessages.logout_ok);
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
    icons: {button: "selectmenu-arrow-icon"}
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

setKotlinVersion = function () {
    $("#kotlinVersionTop").html("(" + KOTLIN_VERSION + ")");
    $("#kotlinVersion").html(WEB_DEMO_VERSION);
    $("#currentYear").html(new Date().getFullYear());
};

setSessionId();
loadShortcuts();
