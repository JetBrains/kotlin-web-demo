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

var editor = new Editor();

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

//WARN
ConfirmDialog.getEditorChangeState = editor.getEditorChangeState;
ConfirmDialog.isLoggedIn = loginView.isLoggedIn;
ConfirmDialog.saveProgram = accordion.saveProgram;


converterModel.addListener("write_exception", consoleView.writeException);
helpModel.addListener("write_exception", consoleView.writeException);
runModel.addListener("write_exception", consoleView.writeException);
accordion.addListener("write_exception", consoleView.writeException);
loginModel.addListener("write_exception", consoleView.writeException);
highlighting.addListener("write_exception", consoleView.writeException);
completion.addListener("write_exception", consoleView.writeException);
editor.addListener("write_exception", consoleView.writeException);

refreshButtonView.addListener("get_highlighting", editor.clearMarkers);
refreshButtonView.addListener("get_highlighting", problemsView.clear);

converterModel.addListener("convert_java_to_kotlin", editor.processConverterResult);
converterModel.addListener("convert_java_to_kotlin", converterView.processConverterResult);
converterModel.addListener("convert_java_to_kotlin", statusBarView.processConverterResult);

helpModel.addListener("help_for_examples", helpView.helpForExamplesLoaded);
helpModel.addListener("help_for_examples", statusBarView.loadHelpForExamples);

helpModel.addListener("help_for_words", helpView.helpForWordsLoaded);
helpModel.addListener("help_for_words", statusBarView.loadHelpForWords);

configuration.addListener("change_configuration", editor.changeConfiguration);
configuration.addListener("change_configuration", runModel.changeConfiguration);
configuration.addListener("change_configuration", consoleView.changeConfiguration);
configuration.addListener("change_configuration", accordion.changeConfiguration);
configuration.addListener("change_configuration", statusBarView.changeConfiguration);

editor.addListener("get_highlighting", highlighting.getHighlighting);
editor.addListener("get_highlighting", statusBarView.loadHighlighting);
editor.addListener("get_completion", completion.getCompletion);
editor.addListener("get_completion", statusBarView.loadCompletion);
editor.addListener("move_cursor", helpView.processCursorActivity);
editor.addListener("move_cursor", statusBarView.processCursorActivity);

highlighting.addListener("get_highlighting", editor.processHighlighting);
highlighting.addListener("get_highlighting", runModel.processHighlighting);
highlighting.addListener("get_highlighting", problemsView.processHighlighting);
highlighting.addListener("get_highlighting", statusBarView.processHighlighting);

completion.addListener("get_completion", editor.processCompletion);
completion.addListener("get_completion", statusBarView.processCompletion);

runModel.addListener("run_java", runButtonView.processRunResult);
runModel.addListener("run_java", consoleView.processOutput);
//runModel.addListener("run_java", problemsView.processOutput);
runModel.addListener("run_java", statusBarView.processOutput);
runModel.addListener("run_js", runButtonView.processRunResult);
runModel.addListener("run_js", consoleView.processOutputForJs);
runModel.addListener("run_js", statusBarView.processOutputForJs);

runModel.addListener("get_highlighting", highlighting.getHighlighting);
runModel.addListener("get_highlighting", statusBarView.loadHighlighting);
RunModel.getProgramText = editor.getProgramText;
RunModel.getArguments = argumentsView.getArguments;


ProgramsView.isLoggedIn = loginView.isLoggedIn;
ProgramsModel.getEditorContent = editor.getProgramText;
ProgramsModel.getArguments = argumentsView.getArguments;

accordion.addListener("load_program", editor.loadExampleOrProgram);
accordion.addListener("load_program", argumentsView.loadExampleOrProgram);
accordion.addListener("load_program", configuration.loadExampleOrProgram);
accordion.addListener("load_program", statusBarView.loadProgram);
accordion.addListener("generate_public_link", statusBarView.generatePublicLink);

accordion.addListener("delete_program", statusBarView.deleteProgram);
accordion.addListener("save_program", editor.resetChangeState);
accordion.addListener("save_program", statusBarView.saveProgram);

accordion.addListener("load_example", configuration.loadExampleOrProgram);
accordion.addListener("load_example", argumentsView.loadExampleOrProgram);
accordion.addListener("load_example", helpView.loadExample);
accordion.addListener("load_example", editor.loadExampleOrProgram);
accordion.addListener("load_example", statusBarView.loadExample);

loginModel.addListener("login", loginView.processUserName);
loginModel.addListener("login", accordion.loadAllContent);
loginModel.addListener("login", statusBarView.login);

loginModel.addListener("logout", loginView.processLogout);
loginModel.addListener("logout", accordion.loadAllContent);
loginModel.addListener("logout", statusBarView.logout);

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



