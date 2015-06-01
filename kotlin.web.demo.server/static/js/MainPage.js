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

var ProjectType = {
    EXAMPLE: "EXAMPLE",
    USER_PROJECT: "USER_PROJECT",
    PUBLIC_LINK: "PUBLIC_LINK"
};

var configurationManager = new ConfigurationComponent();
var differenceDialog = new DifferenceDialogView();

var incompleteActionManager = new IncompleteActionManager();
incompleteActionManager.registerAction("save", "onHeadersLoaded",
    function () {
        localStorage.setItem("contentToSave", JSON.stringify(accordion.getSelectedProject()));
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

var editor = new KotlinEditor();

/**
 * @const
 */
var statusBarView = new StatusBarView(document.getElementById("statusBar"));

var consoleOutputView = new ConsoleOutputView(document.getElementById(console));
var consoleOutputElement = document.createElement("div");
consoleOutputView.writeTo(consoleOutputElement);
consoleOutputView.makeReference = function (fileName, lineNo) {
    var fileView = accordion.getSelectedProjectView().getFileViewByName(fileName);
    if (fileView != null) {
        var a = document.createElement("div");
        a.className = "link";
        if (fileName != null) {
            a.innerHTML = fileName + ':' + lineNo;
        } else {
            a.innerHTML = "Unknown Source";
        }
        a.onclick = function () {
            fileView.fireSelectEvent();
            editor.setCursor(lineNo - 1, 0);
            editor.focus();
        };
        return a;
    } else {
        var span = document.createElement("span");
        if (fileName != null) {
            span.innerHTML = fileName + ':' + lineNo;
        } else {
            span.innerHTML = "Unknown Source";
        }
        return span;
    }
};

$(document).on("click", ".ui-widget-overlay", (function(){
    $(".ui-dialog-titlebar-close").trigger('click');
}));

var generatedCodeView = new Kotlin.modules["kotlin.web.demo.frontend"].views.
    GeneratedCodeView(document.getElementById("generated-code"));
$("#result-tabs").tabs();

var consoleView = new Kotlin.modules["kotlin.web.demo.frontend"].views.ConsoleView(document.getElementById("program-output"), $("#result-tabs"));
var junitView = new JUnitView(document.getElementById("program-output"), $("#result-tabs"));
var problemsView = new ProblemsView(document.getElementById("problems"), $("#result-tabs"));

problemsView.setCursor = function (filename, line, ch) {
    accordion.getSelectedProjectView().getFileViewByName(filename).fireSelectEvent();
    editor.setCursor(line, ch);
    editor.focus();
};


document.getElementById("shortcuts-button").onclick = function(){
    Kotlin.modules["kotlin.web.demo.frontend"].views.dialogs.ShortcutsDialogView.open()};

var helpModelForWords = new Kotlin.modules["kotlin.web.demo.frontend"].providers.HelpProvider();
var helpViewForWords = new Kotlin.modules["kotlin.web.demo.frontend"].views.HelpView(helpModelForWords);
helpViewForWords.hide();


var runProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.RunProvider(
    function (output, project) {
        //TODO remove hack with array
        if(project.confType == "js" ||
        project.confType == "canvas")
            output = output.array;
        $(output).each(function (ind, data) {
            if (data.type == "errors") {
                project.setErrors(data.errors);
                problemsView.addMessages();
                editor.setHighlighting();
            } else if (data.type == "toggle-info" || data.type == "info" || data.type == "generatedJSCode") {
                generatedCodeView.setOutput(data);
            } else {
                if (configurationManager.getConfiguration().type == Configuration.type.JUNIT) {
                    junitView.setOutput(data);
                } else {
                    consoleView.setOutput(data);
                }
            }
        });
        statusBarView.setStatus(ActionStatusMessages.run_java_ok);
    },
    function (data, project) {
        $(data).each(function (ind, data) {
            if (data.type == "errors") {
                project.setErrors(data.errors);
                $("#result-tabs").tabs("option", "active", 0);
                problemsView.addMessages();
                editor.setHighlighting();
                statusBarView.setStatus(ActionStatusMessages.get_highlighting_ok,
                    [getNumberOfErrorsAndWarnings(data.errors)]);
            }
        });
    },
    function () {
        $(runButton).button("option", "disabled", false);
    },
    function (error) {
        consoleView.writeException(error);
        statusBarView.setStatus(ActionStatusMessages.run_java_fail);
    }
);

var loginProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.LoginProvider(
    function () {
        if (accordion.getSelectedFile() != null) {
            accordion.getSelectedFile().save();
        }
        accordion.getSelectedProject().save();
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

var highlightingProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.HighlightingProvider(
    function(data) {
        accordion.getSelectedProject().setErrors(data);
        problemsView.addMessages(data);
        statusBarView.setStatus(ActionStatusMessages.get_highlighting_ok, [getNumberOfErrorsAndWarnings(data)]);
    },
    function(error) {
        unBlockContent();
        consoleView.writeException(error);
        statusBarView.setStatus(ActionStatusMessages.get_highlighting_fail);
    }
);

var completionProvider = (function () {
    var completionProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.CompletionProvider();
    completionProvider.onSuccess = function () {
        statusBarView.setStatus(ActionStatusMessages.get_completion_ok);
    };

    completionProvider.onFail = function (error) {
        consoleView.writeException(error);
        statusBarView.setStatus(ActionStatusMessages.get_completion_fail);
    };

    return completionProvider;
})();


configurationManager.onChange = function (configuration) {
    accordion.getSelectedProject().confType = Configuration.getStringFromType(configuration.type);
    editor.removeHighlighting();
    problemsView.clear();
    editor.updateHighlighting()
};

configurationManager.onFail = function (exception) {
    consoleView.writeException(exception);
    statusBarView.setMessage(ActionStatusMessages.change_configuration_fail);
};

var navBarView = new Kotlin.modules["kotlin.web.demo.frontend"].views.NavBarView(document.getElementById("grid-nav"));

var accordion = (function () {
    var accordion = new AccordionView(document.getElementById("examples-list"));

    accordion.onProjectSelected = function (project) {
        if (project.files.isEmpty()) {
            editor.closeFile();
            if (accordion.getSelectedProject().publicId != getProjectIdFromUrl()) {
                setState(userProjectPrefix + project.publicId, project.name);
            }
            navBarView.onProjectSelected(project);
        }
        consoleView.clear();
        junitView.clear();
        generatedCodeView.clear();
        problemsView.addMessages();
        $("#result-tabs").tabs("option", "active", 0);
        argumentsInputElement.value = project.args;
        configurationManager.updateConfiguration(project.confType);
    };

    accordion.onSelectFile = function (previousFile, currentFile) {
        if (previousFile != null) {
            if (previousFile.project.type != ProjectType.USER_PROJECT) {
                previousFile.project.save();
            } else {
                previousFile.save();
            }
        }

        var url;
        if (currentFile.project.type == ProjectType.EXAMPLE) {
            url = currentFile.id;
        } else if (currentFile.isModifiable) {
            url = userProjectPrefix + accordion.getSelectedProject().publicId + "/" + currentFile.id;
        } else {
            url = userProjectPrefix + accordion.getSelectedProject().publicId + "/" + currentFile.name;
        }
        ;
        setState(url, currentFile.project.name);
        navBarView.onFileSelected(previousFile, currentFile);

        editor.closeFile();
        editor.open(currentFile);
        //currentFile.compareContent();
    };

    accordion.onFail = function (exception, actionCode) {
        consoleView.writeException(exception);
        statusBarView.setMessage(actionCode);
    };

    accordion.onProjectDeleted = function () {
        clearState();
        statusBarView.setStatus(ActionStatusMessages.delete_program_ok);
    };

    accordion.onSaveProgram = function () {
        statusBarView.setStatus(ActionStatusMessages.save_program_ok);
    };

    accordion.onModifiedSelectedFile = function (file) {
        if (file.isModified &&
            file.project.type == ProjectType.PUBLIC_LINK &&
            file.project.revertible) {
            var onProjectExist = function () {
                if (file.isRevertible) {
                    fileProvider.checkFileExistence(
                        file.id,
                        function () {
                            file.isRevertible = false;
                        }
                    )
                }
            };
            var onProjectNotExist = function () {
                file.project.revertible = false;
            };
            projectProvider.checkIfProjectExists(
                file.project.publicId,
                onProjectExist,
                onProjectNotExist
            );
        }
    };

    accordion.onSelectedFileDeleted = function () {
        var project = accordion.getSelectedProject();
        navBarView.onSelectedFileDeleted();
        setState(userProjectPrefix + project.publicId, project.name);
        editor.closeFile();
    };

    return accordion
})();

window.onpopstate = function () {
    var projectId = getProjectIdFromUrl();
    if (accordion.getProjectView(projectId) == null) {
        accordion.loadFirstItem()
    } else {
        if (accordion.getSelectedProject().publicId != projectId) {
            accordion.selectProject(projectId);
        }
        accordion.getSelectedProjectView().selectFileFromUrl();
    }
};

var timer;
editor.onCursorActivity = function (cursorPosition) {
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
};


var runButton = document.getElementById("runButton");
$(runButton)
    .button()
    .click(function () {
        $(runButton).button("option", "disabled", true);
        consoleView.clear();
        junitView.clear();
        generatedCodeView.clear();
        runProvider.run(configurationManager.getConfiguration(), accordion.getSelectedProject());
    });




var fileProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.FileProvider(
    function(){

    },
    function (data) {

        editor.reloadFile();
    }
)

var projectProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.ProjectProvider(
    function () {
        statusBarView.setStatus(ActionStatusMessages.load_project_ok)
    },
    function (name, projectId, fileId) {
        accordion.addNewProject(name, projectId, fileId, null);
    },
    function () {
        statusBarView.setStatus(ActionStatusMessages.load_project_fail)
    }
);

var headersProvider = new Kotlin.modules["kotlin.web.demo.frontend"].providers.HeadersProvider(
    function (message) {
        statusBarView.setStatus(ActionStatusMessages.load_header_fail);
        console.log(message);
    },
    function () {
        statusBarView.setStatus(ActionStatusMessages.load_headers_ok);
    },
    function () {
        statusBarView.setStatus(ActionStatusMessages.load_header_ok);
    },
    function () {
        statusBarView.setStatus(ActionStatusMessages.load_header_fail);
        window.alert("Can't find project, maybe it was removed by the user.");
        clearState();
        accordion.loadFirstItem();
    }
)

//$(document).keydown(function (e) {
//    var shortcut = actionManager.getShortcutByName("org.jetbrains.web.demo.run");
//    if (shortcut.isPressed(e)) {
//        runButton.click();
//    } else {
//        shortcut = actionManager.getShortcutByName("org.jetbrains.web.demo.save");
//        if (shortcut.isPressed(e)) {
//            saveButton.click();
//        }
//    }
//
//});

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


var saveButton = $("#saveButton").click(function () {
    if (accordion.getSelectedProject().type == ProjectType.USER_PROJECT) {
        accordion.getSelectedProject().save();
        if (accordion.getSelectedFile() != null) {
            accordion.getSelectedFile().save();
        }
    } else {
        $("#saveAsButton").click()
    }
});


function openSaveProjectDialog(defaultValue, callback) {
    Kotlin.modules["kotlin.web.demo.frontend"].views.dialogs.InputDialogView.open(
        "Save project",
        "Project name:",
        "Save",
        defaultValue,
        accordion.validateNewProjectName,
        callback
    )
}

$("#saveAsButton").click(function () {
    if (loginView.isLoggedIn) {
        openSaveProjectDialog(
            accordion.getSelectedProject().name,
            projectProvider.forkProject.bind(null, accordion.getSelectedProject(), function (data) {
                accordion.getSelectedProject().loadOriginal();
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
    localStorage.setItem("openedItemId", accordion.getSelectedProject().publicId);

    if (accordion.getSelectedFile() != null) {
        accordion.getSelectedFile().save();
    }
    accordion.getSelectedProject().save();

    localStorage.setItem("highlightOnTheFly", document.getElementById("on-the-fly-checkbox").checked);
    //return null;
};

$("#runMode").selectmenu({
    icons: {button: "selectmenu-arrow-icon"}
});

var argumentsInputElement = document.getElementById("arguments");
argumentsInputElement.oninput = function () {
    accordion.getSelectedProject().args = argumentsInputElement.value;
};

$("#on-the-fly-checkbox")
    .prop("checked", localStorage.getItem("highlightOnTheFly") == "true")
    .on("change", function () {
        var checkbox = document.getElementById("on-the-fly-checkbox");
        editor.highlightOnTheFly(checkbox.checked);
        editor.updateHighlighting();
    });
editor.highlightOnTheFly(document.getElementById("on-the-fly-checkbox").checked);

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

function getKey() {
    $.ajax({
        url: generateAjaxUrl("google_key"),
        type: "GET",
        timeout: 1000,
        success: function (data) {
            console.log(data);
        }
    });
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

FileType = {
    KOTLIN_FILE: "KOTLIN_FILE",
    KOTLIN_TEST_FILE: "KOTLIN_TEST_FILE",
    JAVA_FILE: "JAVA_FILE"
};