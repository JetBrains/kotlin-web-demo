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

var ActionCodes = {
    load_example_ok:StatusBarView.Messages.load_example_fail,
    load_example_fail:StatusBarView.Messages.load_example_fail,
    load_program_ok:StatusBarView.Messages.load_program_ok,
    load_program_fail:StatusBarView.Messages.load_program_fail,
    load_programs_fail:StatusBarView.Messages.load_programs_fail,
    load_examples_fail:StatusBarView.Messages.load_examples_fail,
    generate_link_ok:StatusBarView.Messages.generate_link_ok,
    generate_link_fail:StatusBarView.Messages.generate_link_fail,
    delete_program_ok:StatusBarView.Messages.delete_program_ok,
    delete_program_fail:StatusBarView.Messages.delete_program_fail,
    save_program_ok:StatusBarView.Messages.save_program_ok,
    login_ok:StatusBarView.Messages.login_ok,
    login_fail:StatusBarView.Messages.login_fail,
    save_program_fail:StatusBarView.Messages.save_program_fail
};

var sessionId = -1;
var shortcuts = setShortcuts();
function setShortcuts() {
    if (navigator.appVersion.indexOf("Mac") != -1) {
        return Shortcuts.macShortcuts;
    } else {
        return Shortcuts.winShortcuts;
    }
}

function Example() {
    var name;
    var args;
    var text;
    var runner;
    var dependencies;
    var defaultDependencies;
}

var configurationManager = new ConfigurationComponent();

var editor = new KotlinEditor();

var argumentsView = new ArgumentsView($("#arguments"));
var statusBarView = new StatusBarView($("#statusbar"));
var consoleView = new ConsoleView($("#console"), $("#tabs"));
var problemsView = new ProblemsView($("#problems"), $("#tabs"));

var loginView = new LoginView();
var converterView = new ConverterView($("#javaToKotlin"));
var canvasPopup = new CanvasPopup($("#popupForCanvas"));

var runButton = new Button($("#run"), shortcuts.RUN);
var refreshButton = new Button($("#refresh"), null);

var runProvider = new RunProvider();

var helpModelForExamples = new HelpModel("Examples");
var helpModelForWords = new HelpModel("Words");
var helpViewForExamples = new HelpView("Examples", $("#help1"), helpModelForExamples);
var helpViewForWords = new HelpView("Words", $("#help2"), helpModelForWords);

var loginProvider = new LoginProvider();
var converterProvider = new ConverterModel();

var accordion = new AccordionView($("#examplesaccordion"));
var highlighting = new HighlightingProvider();
var completion = new CompletionProvider();
var loader = new LoaderComponent($('#loader'));

ConfirmDialog.isEditorContentChanged = editor.isEditorContentChanged;
ConfirmDialog.isLoggedIn = loginView.isLoggedIn;
ConfirmDialog.saveProgram = accordion.saveProgram;

refreshButton.onClick = function () {
    refreshButton.setEnabled(false);
    editor.clearMarkers();
    problemsView.clear();
    highlighting.getHighlighting(
        configurationManager.getConfiguration().type,
        editor.getProgramText(),
        function (highlightingResult) {
            editor.addMarkers(highlightingResult);
            refreshButton.setEnabled(true);
        }
    );
};

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

helpModelForExamples.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(StatusBarView.Messages.load_help_for_examples_fail);
};
helpModelForWords.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(StatusBarView.Messages.load_help_for_words_fail);
};

configurationManager.onChange = function (configuration) {
    highlighting.setConfiguration(configuration);
    editor.setConfiguration(configuration);
    consoleView.setConfiguration(configuration);
    accordion.setConfiguration(configuration);
};

configurationManager.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(StatusBarView.Messages.change_configuration_fail);
};

editor.onCursorActivity = function (cursorPosition) {
    var messageForLineAtCursor = editor.getMessageForLineAtCursor(cursorPosition);
    //Save previous message if current is empty
    if (messageForLineAtCursor != "") {
        statusBarView.setMessage(messageForLineAtCursor);
    }
    helpViewForWords.update(editor.getWordAtCursor(cursorPosition));
};

runButton.onClick = function () {
    runButton.setEnabled(false);
    var localConfiguration = configurationManager.getConfiguration();
    highlighting.getHighlighting(localConfiguration.type, editor.getProgramText(), function (highlightingResult) {
        editor.addMarkers(highlightingResult);
        if (!highlighting.checkIfThereAreErrors(highlightingResult)) {
            //Create canvas element before run it in browser
            if (localConfiguration.type == Configuration.type.CANVAS) {
                canvasPopup.show();
            }
            runProvider.run(configurationManager.getConfiguration(), editor.getProgramText(), argumentsView.getArguments());
        } else {
            runButton.setEnabled(true);
        }
    });
};

highlighting.onHighlight = function (highlightingObject) {
    problemsView.addMessages(highlightingObject);
    statusBarView.setMessage(StatusBarView.Messages.get_highlighting_ok);
};

highlighting.onFail = function (error) {
    consoleView.writeException(error);
    statusBarView.setMessage(StatusBarView.Messages.get_highlighting_fail);
};

completion.onComplete = function (completionObject) {
    editor.showCompletionResult(completionObject);
    statusBarView.setMessage(StatusBarView.Messages.get_completion_ok);
};

completion.onFail = function (error) {
    consoleView.writeException(error);
    statusBarView.setMessage(StatusBarView.Messages.get_completion_fail);
};

runProvider.onExecutionFinish = function (output) {
    runButton.setEnabled(true);
    consoleView.setOutput(output);
    statusBarView.setMessage(StatusBarView.Messages.run_java_ok);
};

runProvider.onFail = function (error) {
    runButton.setEnabled(true);
    consoleView.writeException(error);
    statusBarView.setMessage(StatusBarView.Messages.run_java_fail);
};

ProgramsView.isLoggedIn = loginView.isLoggedIn;
ProgramsModel.getEditorContent = editor.getProgramText;
ProgramsModel.getArguments = argumentsView.getArguments;


accordion.onFail = function (exception, actionCode) {
    consoleView.writeException(exception);
    statusBarView.setMessage(actionCode);
};
accordion.onLoadCode = function (element, type) {
    if (type == "example") {
        helpViewForExamples.update(element.name);
        statusBarView.setMessage(StatusBarView.Messages.load_example_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.load_program_ok);
    }
    editor.setText(element.text);
    argumentsView.setArgs(element.args);
    configurationManager.updateConfiguration(getFirstConfiguration(element.confType));
};

accordion.onGeneratePublicLink = function () {
    statusBarView.setMessage(StatusBarView.Messages.generate_link_ok);
};

accordion.onLoadAllContent = function () {
    loader.hide();
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
    loginView.logout();
    statusBarView.setMessage(StatusBarView.Messages.logout_ok);
    accordion.loadAllContent();
};

loginProvider.onFail = function (exception, actionCode) {
    consoleView.writeException(exception);
    statusBarView.setMessage(actionCode);
};

$(document).keydown(function (e) {
    if (navigator.appVersion.indexOf("Mac") != -1) {
        if (e.keyCode == 82 && e.ctrlKey) {
            runButton.click();
        } else if (e.keyCode == 83 && e.metaKey) {
            if (e.preventDefault) e.preventDefault();
            else e.returnValue = false;
            accordion.saveProgram();
        }
    } else {
        if (e.keyCode == 120 && e.ctrlKey) {
            runButton.click();
        } else if (e.keyCode == 83 && e.ctrlKey) {
            if (e.preventDefault) e.preventDefault();
            else e.returnValue = false;
            accordion.saveProgram();
        }
    }
});

/*if (e.keyCode == 120 && e.ctrlKey && e.shiftKey) {
 //runConfiguration.mode = "js";
 //$("#runConfigurationMode").selectmenu("value", "js");
 $("#run").click();
 } else */

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

function loadShortcuts() {
    var text = $("#help3").html();
    text = text.replace("@shortcut-autocomplete@", shortcuts.AUTOCOMPLETE);
    text = text.replace("@shortcut-run@", shortcuts.RUN);
    text = text.replace("@shortcut-reformat@", shortcuts.REFORMAT_CODE);
    text = text.replace("@shortcut-save@", shortcuts.SAVE);
    $("#help3").html(text);
}

$("#whatimgjavatokotlin").click(function () {
    $("#dialogAboutJavaToKotlinConverter").dialog("open");
});

$("#dialogAboutJavaToKotlinConverter").dialog({
    modal:"true",
    width:300,
    autoOpen:false
});

function generateAjaxUrl(type, args) {
    var url = [location.protocol, '//', location.host, "/"].join('');
    return url + "kotlinServer?sessionId=" + sessionId + "&type=" + type + "&args=" + args;
}

function setSessionId() {
    $.ajax({
        url:generateAjaxUrl("getSessionId", "null"),
        context:document.body,
        type:"GET",
        dataType:"json",
        timeout:10000,
        success:getSessionIdSuccess
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
        url:generateAjaxUrl("sendUserData", "null"),
        context:document.body,
        type:"POST",
        data:{text:info},
        timeout:5000
    });
}

$("#popupForCanvas").dialog({
    width:630,
    height:350,
    autoOpen:false,
    close:function () {
        $("#popupForCanvas").html("");
    }
});

setSessionId();
loadShortcuts();
