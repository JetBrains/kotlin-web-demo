var arrayClasses = [];
var arrayLinesMarkers = [];

function removeStyles() {
    var i = 0;
    while (typeof arrayClasses[i] != "undefined") {
        arrayClasses[i].clear();
        i++;
    }
    i = 0;
    while (typeof arrayLinesMarkers[i] != "undefined") {
        editor.clearMarker(arrayLinesMarkers[i]);
        i++;
    }


    /*function remove(i, f) {
     document.getElementById("debug").innerHTML += " i " + i  + " lenght class " + arrayClasses.length + " marker " + arrayLinesMarkers.length + "<br/>";
     if (typeof arrayClasses[i] == "undefined") {
     return;
     }
     if (typeof arrayLinesMarkers[i] != "undefined") {
     editor.clearMarker(arrayLinesMarkers[i]);
     }
     arrayClasses[i].clear();
     i++;
     setTimeout(function (i) {
     return function () {
     f(i, remove);
     }
     }(i), 10);
     }

     remove(0, remove);*/
}


var timer;
var timerIntervalForNonPrinting = 300;

var isCompletionInProgress = false;

var keywords;
var isContinueComplete = false;

var isGetErrorsAfterRunInNoHighlightingMode = false;

editor = CodeMirror.fromTextArea(document.getElementById("code"), {
    lineNumbers:true,
    matchBrackets:true,
    mode:"text/kotlin",
    extraKeys:{
        "Ctrl-Space":beforeComplete/*,
         "Ctrl-Shift-F9":function (instance) {
         $("#runJS").click();
         },
         "Ctrl-F9":function (instance) {
         $("#run").click();
         }*/
    },
    onChange:runTimerForNonPrinting,
    onCursorActivity:updateStatusBar,
    minHeight:"430px",
    tabSize:2
});

function runTimerForNonPrinting() {
    setEditorState(true);
    if (timer) {
        clearTimeout(timer);
        timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
    }
    else {
        timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
    }
}

function getHighlighting() {
    if (!isCompletionInProgress && !Accordion.checkIfLoading() && !Highlighting.isLoadingErrors()) {
        getErrors();
    }
}

var time;

function getErrors() {
    Highlighting.getErrors(Highlighting.onHighlightingSuccess);
}


var isFirstTryToLoadApplet = true;

function getDataFromApplet(type) {
    Highlighting.getDataFromApplet(type);
}

function updateStatusBar() {
    updateHelp(editor.getTokenAt(editor.getCursor(true)).string);
    var lineNumber = editor.getCursor(true).line;
    var text = editor.lineInfo(lineNumber).markerText;
    if (text != null) {
        text = text.substring(text.indexOf("title=\"") + 7);
        text = text.substring(0, text.indexOf("\""));
        if (text.length > 90) text = text.substring(0, 90) + "...";
        setStatusBarMessage("line " + (lineNumber + 1) + " - " + text);
    } else {
        setStatusBarMessage("");
    }
}

$("#arguments").val("");

function checkIfThereAreErrorsInProblemView() {
    var children = document.getElementById("problems").childNodes;
    var result = false;
    if (children.length > 0) {
        for (var i = 0; i < children.length; ++i) {
            if (children[i].className == "problemsViewError") {
                result = true;
                break;
            }
        }
        if (result) {
            setStatusBarMessage(ERROR_UNTIL_EXECUTE);
            setConsoleMessage(TRY_RUN_CODE_WITH_ERROR);
        }
    }
    return result;
}

function checkIfThereAreErrorsInData(data) {
    var i = 0;
    while (typeof data[i] != "undefined") {
        var severity = data[i].severity;
        if (severity == "ERROR") {
            return true;
        }
        i++;
    }
    return false;
}

function createRedElement(text) {
    var div = document.createElement("div");
    var p = document.createElement("p");
    p.className = "consoleViewError";
    p.innerHTML = text;
    div.appendChild(p);
    return div.innerHTML;
}

$("#refreshGutters").click(function () {
    setRunConfigurationMode();
    setStatusBarMessage("Loading...");
    clearProblemView();
    removeStyles();
    for (var i = 0; i < editor.lineCount(); i++) {
        try {
            editor.clearMarker(i);
        } catch (e) {
            //Absent marker for line
        }
    }
    var isChecked = false;
    if ($("#nohighlightingcheckbox").attr('checked') == 'checked') {
        $("#nohighlightingcheckbox").attr('checked', false);
        isChecked = true;
    }
    getErrors();
    if (isChecked) {
        $("#nohighlightingcheckbox").attr('checked', true);
    }
});


$("#run").click(function () {
    Runner.run();
});


function exception(ex) {
    var statusMes = "";
    var pos = ex.exception.indexOf("<br/>");
    if (pos != -1) {
        statusMes = unEscapeString(ex.exception.substr(0, pos));
    }

    var problems = document.createElement("div");
    if (ex.type == "out") {
        document.getElementById("problems").appendChild(createElementForProblemView("STACKTRACE", null, unEscapeString(ex.exception)));
    } else {
        document.getElementById("problems").appendChild(createElementForProblemView("ERROR", null, unEscapeString(ex.exception)));
    }
}

function createElementForProblemView(severity, start, title) {
    var p = document.createElement("p");
    var img = document.createElement("img");
    if (severity == 'WARNING') {
        img.src = "/icons/warning.png";
        if (title.indexOf("is never used") > 0) {
            p.className = "problemsViewWarningNeverUsed";
        } else {
            p.className = "problemsViewWarning";
        }

    } else if (severity == 'STACKTRACE') {
        p.className = "problemsViewStacktrace";
    } else {
        img.src = "/icons/error.png";
        p.className = "problemsViewError";
    }
    p.appendChild(img);
    var titleDiv = document.createElement("span");
    if (start == null) {
        titleDiv.innerHTML = " " + unEscapeString(title);
    } else {

        titleDiv.innerHTML = "(" + (start.line + 1) + ", " + (start.ch + 1) + ") : " + unEscapeString(title);
    }
    p.appendChild(titleDiv);
    return p;
}

function beforeComplete() {
    runTimerForNonPrinting();
    if ($("#nohighlightingcheckbox").attr('checked') == 'checked') {
        startComplete(COMPLETION_ISNOT_AVAILABLE);
        setStatusBarMessage(COMPLETION_ISNOT_AVAILABLE);
        return;
    }
    if (!isCompletionInProgress) {
        isCompletionInProgress = true;
        var i = editor.getValue();
        if (isApplet) {
            getDataFromApplet("complete");
            isCompletionInProgress = false;
        } else {
            $.ajax({
                url:generateAjaxUrl("complete", editor.getCursor(true).line + "," + editor.getCursor(true).ch + "&runConf=" + runConfiguration.mode),
                context:document.body,
                success:startComplete,
                dataType:"json",
                type:"POST",
                data:{text:i},
                timeout:10000,
                error:function () {
                    isCompletionInProgress = false;
                    setStatusBarMessage(COMPLETE_REQUEST_ABORTED);
                }
            });
        }
    }
}

function LookupElement(name, tail, icon) {
    this.icon = icon;
    this.name = name;
    this.tail = tail;
}

function continueComplete() {
    isContinueComplete = true;
    startComplete(null);
}

function startComplete(data) {
    isCompletionInProgress = false;
    if (editor.somethingSelected()) return;
    var cur = editor.getCursor(null);
    var token = editor.getTokenAt(cur);

    if ((data != null) && (typeof data != "undefined")) {
        if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
            exception(data[0]);
            return;
        }
        if (!isContinueComplete) {
            keywords = [];
        }
        if (data == COMPLETION_ISNOT_AVAILABLE) {
            keywords.push(new LookupElement(COMPLETION_ISNOT_AVAILABLE, "", ""));
        } else {
            var i = 0;
            while (typeof data[i] != "undefined") {
                var lookupElement = new LookupElement(data[i].name, data[i].tail, data[i].icon)
                keywords.push(lookupElement);
                i++;
            }
        }
    } else {
        if (!isContinueComplete) {
            keywords = [];
            return;
        }
        isContinueComplete = false;
    }
    var completions;
    if (data == COMPLETION_ISNOT_AVAILABLE) {
        completions = getCompletions(COMPLETION_ISNOT_AVAILABLE);
    } else {
        completions = getCompletions(token);
    }
    if ((completions.length == 0) || (completions == null)) return;
    if (completions.length == 1 && !isContinueComplete && data != COMPLETION_ISNOT_AVAILABLE) {
        insert(completions[0].name);
        return;
    }

    function insert(str) {
        if (typeof str != "undefined") {
            var position = str.indexOf("(");
            if (position != "undefined") {
                if (position != -1) {
                    //If this is a string with a package after
                    if (str.charAt(position - 1) == ' ') {
                        position = position - 2;
                    }
                    //if this is a method without args
                    if (str.charAt(position + 1) == ')') {
                        position++;
                    }
                    str = str.substring(0, position + 1);
                }
            }
            position = str.indexOf(":");
            if (position != "undefined") {
                if (position != -1) {
                    str = str.substring(0, position - 1);
                }
            }
            if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                editor.replaceRange(str, {line:cur.line, ch:token.end}, {line:cur.line, ch:token.end});
            } else {
                editor.replaceRange(str, {line:cur.line, ch:token.start}, {line:cur.line, ch:token.end});
            }
        }
    }

    // Build the select widget
    var complete = document.createElement("div");
    complete.id = "complete";
    complete.className = "completions";

    var sel = document.createElement("select");
    sel.id = "selectId";
    for (i = 0; i < completions.length; ++i) {
        var opt = document.createElement("option");
        var pEl = document.createElement("p");
        pEl.className = "lookupElement";

        var icon = document.createElement("img");
        icon.className = "lookupElementIcon";
        icon.src = completions[i].icon;
        pEl.appendChild(icon);

        var spanName = document.createElement("div");
        spanName.className = "lookupElementName";
        spanName.innerHTML = completions[i].name;

        var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
        if (is_chrome && data != COMPLETION_ISNOT_AVAILABLE) {
            spanName.innerHTML += " : ";
        }
        pEl.appendChild(spanName);

        var spanTail = document.createElement("div");
        spanTail.className = "lookupElementTail";
        spanTail.innerHTML = completions[i].tail;
        pEl.appendChild(spanTail);
        opt.appendChild(pEl);
        if (i == 0) {
            opt.id = "selected";
        }
        sel.appendChild(opt);
    }

    complete.appendChild(sel);


    sel.multiple = true;

    if (data == COMPLETION_ISNOT_AVAILABLE) {
        sel.firstChild.selected = false;
    }
    sel.size = Math.min(10, i);

    var pos = editor.cursorCoords();
    complete.style.left = pos.x + "px";
    complete.style.top = pos.yBot + "px";
    document.body.appendChild(complete);
    document.getElementById("complete").focus();
//        $("select").selectmenu();


    // Hack to hide the scrollbar.
    if (i <= 10) {
        complete.style.width = (sel.clientWidth - 1) + "px";
        complete.style.height = (sel.size * 18) + "px";
    }

    var done = false;

    function close() {
        if (done) return;
        done = true;
        complete.parentNode.removeChild(complete);
    }

    function pick() {
        var text = sel.options[sel.selectedIndex].childNodes[0].childNodes[1].textContent;
        if (typeof text == "undefined") {
            text = sel.options[sel.selectedIndex].childNodes[0].childNodes[1].innerHTML;
        }
        if (text == COMPLETION_ISNOT_AVAILABLE) {
            close();
            editor.focus();
            return;
        }
        insert(text);
        close();
        setTimeout(function () {
            editor.focus();
        }, 50);
    }

    connect(sel, "blur", close);
    connect(sel, "keydown", function (event) {
        var code = event.keyCode;

        // Enter and space
        if (code == 13 || code == 32) {
            isContinueComplete = false;
            event.stop();
            pick();
        }
        // Escape
        else if (code == 27) {
            isContinueComplete = false;
            event.stop();
            close();
            editor.focus();
        } else if (code == 8) {
            event.stop();
            close();
            editor.focus();
            editor.deleteH(-1, "char");
            setTimeout(continueComplete, 50);
        } else if (code != 38 && code != 40) {
            close();
            editor.focus();
            setTimeout(continueComplete, 50);
        }
    });


    connect(sel, "dblclick", pick);

    sel.focus();
    // Opera sometimes ignores focusing a freshly created node
    if (window.opera) setTimeout(function () {
        if (!done) sel.focus();
    }, 100);
}

function getCompletions(token) {
    var start;
    if (token == COMPLETION_ISNOT_AVAILABLE) {
        start = token;
    } else {
        start = token.string;
    }
    var found = [];

    function maybeAdd(lookupElement) {
        if (lookupElement.name.indexOf(start) == 0) found.push(lookupElement);
    }

    function add(str) {
        found.push(str);
    }

    if (typeof keywords == "undefined") return found;
    if ((start.indexOf(' ') == 0) || (start == '.')) {
        forEachInArray(keywords, add);
    } else {
        forEachInArray(keywords, maybeAdd);
    }

    return found;
}

/*String.prototype.hashCode = function () {
 var hash = 0;
 if (this.length == 0) return hash;
 for (i = 0; i < this.length; i++) {
 char = this.charCodeAt(i);
 hash = ((hash << 5) - hash) + char;
 hash = hash & hash; // Convert to 32bit integer
 }
 return hash;
 }*/

// Minimal event-handling wrapper.
function stopEvent() {
    if (this.preventDefault) {
        this.preventDefault();
        this.stopPropagation();
    }
    else {
        this.returnValue = false;
        this.cancelBubble = true;
    }
}

function addStop(event) {
    if (!event.stop) event.stop = stopEvent;
    return event;
}

function connect(node, type, handler) {
    function wrapHandler(event) {
        handler(addStop(event || window.event));
    }

    if (typeof node.addEventListener == "function")
        node.addEventListener(type, wrapHandler, false);
    else
        node.attachEvent("on" + type, wrapHandler);
}

function forEach(arr, f) {
    for (var i = 0, e = arr.length; i < e; ++i) f(arr[i]);
}

function forEachInArray(arr, f) {
    var i = 0;
    while (arr[i] != undefined) {
        f(arr[i]);
        i++;
    }
}

//$("#myapplet")[0].getHighlighting("fun main(args : Array<String>) { System.out?.println(\"Hello, world!\")}");


function sendHighlightingRequest(onLoad) {
    Highlighting.sendHighlightingRequest(onLoad);
}



