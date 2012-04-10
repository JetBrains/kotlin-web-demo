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


function Example() {
    var name;
    var args;
    var text;
    var runner;
    var dependencies;
    var defaultDependencies;
}

var configuration = new ConfigurationComponent();
var requestGenerator = new RequestGenerator();

var editor = new KotlinEditor();

var argumentsView = new ArgumentsView();
var statusBarView = new StatusBarView();
var consoleView = new ConsoleView();
var problemsView = new ProblemsView();
var helpView = new HelpView();
var runButtonView = new RunButtonView();
var loginView = new LoginView();
var converterView = new ConverterView();
var refreshButtonView = new RefreshButtonView();

var runModel = new RunModel();
var helpModel = new HelpModel();
var loginModel = new LoginModel();
var converterModel = new ConverterModel();

var accordion = new AccordionView();
var highlighting = new HighlightingProvider();
var completion = new CompletionProvider();
var loader = new LoaderComponent();

ConfirmDialog.getEditorChangeState = editor.getEditorChangeState;
ConfirmDialog.isLoggedIn = loginView.isLoggedIn;
ConfirmDialog.saveProgram = accordion.saveProgram;


refreshButtonView.onRefresh = function () {
    editor.clearMarkers();
    problemsView.clear();
};

converterModel.onConvert = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        editor.refreshMode();
        editor.setText(data[0].text);
        editor.indentAll();
        converterView.closeDialog();
        statusBarView.setMessage(StatusBarView.Messages.convert_java_to_kotlin_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.convert_java_to_kotlin_ok);
    }
};

helpModel.onHelpForExamplesLoaded = function (status, data) {
    if (status && checkDataForNull(data)) {
        helpView.helpForExamplesLoaded(data);
        statusBarView.setMessage(StatusBarView.Messages.load_help_for_examples_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.load_help_for_examples_fail);
    }
};

helpModel.onHelpForWordsLoaded = function (status, data) {
    if (status && checkDataForNull(data)) {
        helpView.helpForWordsLoaded(data);
        statusBarView.setMessage(StatusBarView.Messages.load_help_for_words_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.load_help_for_words_fail);
    }
};

configuration.onChangeConfiguration = function (status, data) {
    if (status && checkDataForNull(data)) {
        editor.setConfiguration(data);
        runModel.setConfiguration(data);
        consoleView.setConfiguration(data);
        accordion.setConfiguration(data);
        statusBarView.setMessage(StatusBarView.Messages.change_configuration_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.change_configuration_fail);
    }
};

editor.moveCursor = function (data) {
    if (checkDataForNull(data)) {
        if (data[0] != "") {
            statusBarView.setMessage(data[0]);
        }
        helpView.changeHelpForWord(data[1]);
    }
};

highlighting.onHighlight = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        editor.addErrors(data);
        problemsView.addErrors(data);
        //TODO
        runModel.processHighlighting(data);
        statusBarView.setMessage(StatusBarView.Messages.get_highlighting_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.get_highlighting_fail);
    }
};

completion.onComplete = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        editor.addCompletionWidget(data);
        statusBarView.setMessage(StatusBarView.Messages.get_completion_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.get_completion_fail);
    }
};

runModel.onRun = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        runButtonView.setVisible();
        consoleView.setOutput(data);
        statusBarView.setMessage(StatusBarView.Messages.run_java_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.run_java_fail);
    }
};

runModel.onRunJs = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        runButtonView.setVisible();
        consoleView.setOutputForJs(data);
        statusBarView.setMessage(StatusBarView.Messages.run_js_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.run_js_fail);
    }
};

RunModel.getProgramText = editor.getProgramText;
RunModel.getArguments = argumentsView.getArguments;

ProgramsView.isLoggedIn = loginView.isLoggedIn;
ProgramsModel.getEditorContent = editor.getProgramText;
ProgramsModel.getArguments = argumentsView.getArguments;


accordion.onLoadProgram = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        editor.setText(data[0].text);
        argumentsView.setArgs(data[0].args);
        configuration.updateRunnerAndDependencies(substringDependencies(data[0].dependencies), substringDependencies(data[0].dependencies));
        statusBarView.setMessage(StatusBarView.Messages.load_program_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.load_program_fail);
    }
};

accordion.onLoadExample = function (status, data) {
    if (status && checkDataForNull(data) && checkDataForException(data)) {
        helpView.loadHelpForExample(data[0].name);
        editor.setText(data[0].text);
        argumentsView.setArgs(data[0].args);
        configuration.updateRunnerAndDependencies(substringDependencies(data[0].dependencies), substringDependencies(data[0].dependencies));
        statusBarView.setMessage(StatusBarView.Messages.load_example_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.load_example_fail);
    }
};
accordion.onPublicLinkGenerated = function (status) {
    if (status) {
        statusBarView.setMessage(StatusBarView.Messages.generate_link_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.generate_link_fail);
    }
};

accordion.onLoadAllContent = function () {
    loader.hide();
};

accordion.onDeleteProgram = function (status) {
    if (status) {
        statusBarView.setMessage(StatusBarView.Messages.delete_program_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.delete_program_fail);
    }
};

accordion.onSaveProgram = function (status) {
    if (status) {
        editor.resetChangeState();
        statusBarView.setMessage(StatusBarView.Messages.save_program_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.save_program_fail);
    }
};

loginModel.onLogin = function (status, data) {
    if (status) {
        loginView.setUserName(data);
        statusBarView.setMessage(StatusBarView.Messages.login_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.login_fail);
    }
    accordion.loadAllContent();
};

loginModel.onLogout = function (status, data) {
    if (status) {
        loginView.logout();
        statusBarView.setMessage(StatusBarView.Messages.logout_ok);
    } else {
        statusBarView.setMessage(StatusBarView.Messages.logout_fail);
    }
    accordion.loadAllContent();
};

function checkDataForNull(data) {
    if (data == null || data == undefined) {
        statusBarView.setMessage("Received data is null");
        return false;
    }
    return true;
}

function checkDataForException(data) {
    if (data[0] != null && data[0] == undefined && data[0].exception != undefined) {
        consoleView.writeException(data);
        return false;
    }
    return true;
}

$(document).keydown(function (e) {
    if (navigator.appVersion.indexOf("Mac") != -1) {
        if (e.keyCode == 82 && e.ctrlKey) {
            runButtonView.buttonClick();
        } else if (e.keyCode == 83 && e.metaKey) {
            if (e.preventDefault) e.preventDefault();
            else e.returnValue = false;
            accordion.saveProgram();
        }
    } else {
        if (e.keyCode == 120 && e.ctrlKey) {
            runButtonView.buttonClick();
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



