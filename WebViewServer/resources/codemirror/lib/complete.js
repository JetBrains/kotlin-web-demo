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


$(document).ready(function () {

    $("#myapplet")[0].getHighlighting("fun main(args : Array<String>) { System.out?.println(\"Hello, world!\")}");

    var timer;
    var timerIntervalForNonPrinting = 300;

    var isCompletionInProgress = false;
    var isCompilationInProgress = false;
    var isLoadingHighlighting = false;

    var keywords;
    var isContinueComplete = false;

    editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers:true,
        matchBrackets:true,
        mode:"text/kotlin",
        extraKeys:{
            "Ctrl-Space":beforeComplete,
            "Ctrl-F9":function (instance) {
                $("#run").click();
            },
            "Ctrl-Shift-F9":function (instance) {
                $("#runJS").click();
            }
        },
        onChange:runTimerForNonPrinting,
        onCursorActivity:updateStatusBar,
        minHeight:"430px",
        tabSize:2
    });

    function runTimerForNonPrinting() {
        isContentEditorChanged = true;
        if (timer) {
            clearTimeout(timer);
            timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
        }
        else {
            timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
        }
    }

    function getHighlighting() {
        $("#tabs").tabs("select", 0);
        if (!isCompletionInProgress && !loadingExample && !isLoadingHighlighting) {
            getErrors();
        }
    }

    var time;

    function getErrors() {
        time = new Date().getMilliseconds();
        isLoadingHighlighting = true;
        if (isApplet) {
            getDataFromApplet("highlighting");
            isLoadingHighlighting = false;
        } else {
//            document.getElementById("debug").innerHTML = " " + (new Date().getMilliseconds() - time);
            var i = editor.getValue();
            $.ajax({
                url:document.location.href + "?sessionId=" + sessionId + "&sendData=true",
                context:document.body,
                success:onHighlightingSuccess,
                dataType:"json",
                type:"POST",
                data:{text:i},
                timeout:10000,
                error:function () {
                    isLoadingHighlighting = false;
                    setConsoleMessage(REQUEST_ABORTED);
                }
            });
        }
    }


    function onHighlightingSuccess(data) {
        isLoadingHighlighting = false;
        if (data == null) {
            return;
        }
        removeStyles();
        arrayClasses = [];
        arrayLinesMarkers = [];

        if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
            document.getElementById("problems").innerHTML = "";
            setStatusBarMessage(unEscapeString(data[0].exception));
            setConsoleMessage(unEscapeString(data[0].exception));
            var j = 0;
            while (typeof data[j] != "undefined") {
                exception(data[j]);
                j++;
            }
            return;
        }

        var i = 0;
        var problems = document.createElement("div");

        function processError(i, p, f) {
            if (typeof data[i] == "undefined") {
                document.getElementById("problems").innerHTML = problems.innerHTML;
                updateStatusBar();
                return;
            }
            arrayClasses.push(editor.markText(eval('(' + data[i].x + ')'), eval('(' + data[i].y + ')'), data[i].className));

            var title = unEscapeString(data[i].titleName);
            var start = eval('(' + data[i].x + ')');
            var severity = data[i].severity;

            if ((editor.lineInfo(start.line) != null) && (editor.lineInfo(start.line).markerText == null)) {
                editor.setMarker(start.line, '<span class=\"' + severity + 'gutter\" title="' + title + '">  </span>%N%');
                this.arrayLinesMarkers.push(start.line);
            } else {
                var text = editor.lineInfo(start.line).markerText;
                var resultSpan = "";
                if ((severity == "WARNING") && text.indexOf("ERRORgutter") != -1) {
                    text = text.substring(text.indexOf("title=\"") + 7);
                    text = text.substring(0, text.indexOf("\""));
//                    resultSpan += title + "\n ---next error--- \n" + text.substring(pos);
                    resultSpan = '<span class=\"ERRORgutter\" title="' + text + "\n ---next error--- \n" + title + '">  </span>%N%';
                } else {
                    text = text.substring(text.indexOf("title=\"") + 7);
                    text = text.substring(0, text.indexOf("\""));
                    resultSpan = '<span class=\"' + severity + 'gutter\" title="' + text + "\n ---next error--- \n" + title + '">  </span>%N%';
                }
                this.arrayLinesMarkers.pop();
                editor.setMarker(start.line, resultSpan);
                this.arrayLinesMarkers.push(start.line);
            }

            var el = document.getElementById(start.line + "_" + start.ch);
            if (el != null) {
                el.setAttribute("title", title);
            }

            //add exception at problemsView
            var problem = createElementForProblemView(severity, start, title);
            problems.appendChild(problem);
            i++;
            setTimeout(function (i, problems) {
                return function () {
                    f(i, problems, processError);
                }
            }(i, problems), 10);
        }

        processError(i, problems, processError);
    }

    function getDataFromApplet(type) {
        var i = editor.getValue();
        try {
            var dataFromApplet;
            try {
                if (type == "complete") {
                    dataFromApplet = $("#myapplet")[0].getCompletion(i, editor.getCursor(true).line, editor.getCursor(true).ch);
                } else {
                    dataFromApplet = $("#myapplet")[0].getHighlighting(i);
                }
            } catch (e) {
                $(".applet-disable").click();
                setStatusBarMessage(GET_FROM_APPLET_FAILED);
                if (type == "highlighting") {
                    isLoadingHighlighting = false;
                    getErrors();
                }
                return;
            }
            var data = eval(dataFromApplet);
            if (typeof data != "undefined") {
                if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
                    $("#tabs").tabs("select", 0);
                    document.getElementById("problems").innerHTML = "";
                    setStatusBarMessage(data[0].exception);
                    var j = 0;
                    while (typeof data[j] != "undefined") {
                        exception(data[j]);
                        j++;
                    }
                } else {
                    if (type == "complete") {
                        startComplete(data);
                    } else {
                        onHighlightingSuccess(data);
                    }
                }
            }
        } catch (e) {
            isLoadingHighlighting = false;
            isCompletionInProgress = false;
        }
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
        getErrors();
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
                document.getElementById("console").innerHTML = TRY_RUN_CODE_WITH_ERROR;
            }
        }
        return result;
    }

    function createRedElement(text) {
        var div = document.createElement("div");
        var p = document.createElement("p");
        p.className = "consoleViewError";
        p.innerHTML = text;
        div.appendChild(p);
        return div.innerHTML;
    }


    $("#run").click(function () {
        var arguments = $("#arguments").val();
        var i = editor.getValue();
        $("#tabs").tabs("select", 1);
        setConsoleMessage("");
        setStatusBarMessage("Running...");
        getErrors();
        setStatusBarMessage("Running...");
        if (!isCompilationInProgress && !checkIfThereAreErrorsInProblemView()) {
            isCompilationInProgress = true;

            $.ajax({
                url:document.location.href + "?sessionId=" + sessionId + "&run=true",
                context:document.body,
                success:onCompileSuccess,
                dataType:"json",
                type:"POST",
                data:{text:i, consoleArgs:arguments},
                timeout:10000,
                error:function () {
                    isCompilationInProgress = false;
                    setStatusBarMessage(REQUEST_ABORTED);
                    document.getElementById("console").innerHTML = REQUEST_ABORTED;
                }
            });

        }
    });

    $("#runJS").click(function () {
        var arguments = $("#arguments").val();
        var i = editor.getValue();
        $("#tabs").tabs("select", 1);
        setConsoleMessage("");
        setStatusBarMessage("Running...");
        getErrors();
        setStatusBarMessage("Running...");
        if (!isCompilationInProgress && !checkIfThereAreErrorsInProblemView()) {
            isCompilationInProgress = true;
            if (isApplet && isJsApplet) {
                try {
                    var dataFromApplet;
                    try {
                        dataFromApplet = $("#jsapplet")[0].translateToJS(i, arguments);
                    } catch (e) {
                        loadJsFromServer(i, arguments);
                        return;
                    }
                    var isCompilationInProgress = false;
                    var data;
                    if (dataFromApplet.indexOf("exception=") == 0) {
                        data = dataFromApplet.substring(10, dataFromApplet.length);
                        data = createRedElement(COMPILE_IN_JS_APPLET_ERROR + "<br/>" + data);
                        setStatusBarMessage(ERROR_UNTIL_EXECUTE);
                        setConsoleMessage("<p>" + data + "</p>");
                    } else {
                        data = eval(dataFromApplet);
                        setStatusBarMessage(EXECUTE_OK);
                        generatedJSCode = dataFromApplet;
                        setConsoleMessage("<p>" + data + "</p><p class='consoleViewInfo'><a href='javascript:void(0);' onclick='showJsCode();'>" + SHOW_JAVASCRIPT_CODE + "</a></p>");
                    }
                } catch (e) {
                    setStatusBarMessage(ERROR_UNTIL_EXECUTE);
                    setConsoleMessage(createRedElement(COMPILE_IN_JS_APPLET_ERROR + "<br/>" + e));
                }
            } else {
                loadJsFromServer(i, arguments);
            }

        }
    });

    function loadJsFromServer(i, arguments) {
        isJsApplet = false;
        $.ajax({
            url:document.location.href + "?sessionId=" + sessionId + "&convertToJs=true",
            context:document.body,
            success:onConvertToJsSuccess,
            dataType:"json",
            type:"POST",
            data:{text:i, consoleArgs:arguments},
            timeout:10000,
            error:function () {
                isCompilationInProgress = false;
                setStatusBarMessage(REQUEST_ABORTED);
                document.getElementById("console").innerHTML = REQUEST_ABORTED;
            }
        });
    }

    function onConvertToJsSuccess(data) {
        var genData;
        if (data != null) {
            if (typeof data[0].exception != "undefined") {
                genData = createRedElement(COMPILE_IN_JS_APPLET_ERROR + "<br/>" + data[0].exception);
                setStatusBarMessage(ERROR_UNTIL_EXECUTE);
                setConsoleMessage("<p>" + genData + "</p>");
            } else if (typeof data[0].text != "undefined") {
                genData = eval(data[0].text);
                setStatusBarMessage(EXECUTE_OK);
                generatedJSCode = data[0].text;
                setConsoleMessage("<p>" + genData + "</p><p class='consoleViewInfo'><a href='javascript:void(0);' onclick='showJsCode();'>" + SHOW_JAVASCRIPT_CODE + "</a></p>");
            }

        }
    }

    function onCompileSuccess(data) {
        var isCompiledWithErrors = false;
        isCompilationInProgress = false;
        if (data != null) {
            if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
                $("#tabs").tabs("select", 0);
                document.getElementById("problems").innerHTML = "";
                setStatusBarMessage(data[0].exception);
                var j = 0;
                while (typeof data[j] != "undefined") {
                    exception(data[j]);
                    j++;
                }
                return;
            } else {
                var i = 0;
                var errors = document.createElement("div");
                while (typeof data[i] != "undefined") {
                    //If there is a compilation error
                    if (typeof data[i].message != "undefined") {
                        getErrors();
                        isCompiledWithErrors = true;
                        errors.appendChild(createElementForProblemView(data[i].type, null, data[i].message));
                    } else {
                        var p = document.createElement("p");
                        if ((data[i].type == "err") && (data[i].text != "")) {
                            p.className = "consoleViewError";
                            isCompiledWithErrors = true;
                        }
                        if (data[i].type == "info") {
                            p.className = "consoleViewInfo";
                        }
                        p.innerHTML = data[i].text;
                        errors.appendChild(p);
                    }
                    i++;
                }
                document.getElementById("console").appendChild(errors);
            }
        }
        if (!isCompiledWithErrors) {
            setStatusBarMessage(EXECUTE_OK);
        } else {
            setStatusBarMessage(ERROR_UNTIL_EXECUTE);
        }
    }

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
            p.className = "problemsViewWarning";
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
        if (!isCompletionInProgress) {
            isCompletionInProgress = true;
            var i = editor.getValue();
            if (isApplet) {
                getDataFromApplet("complete");
                isCompletionInProgress = false;
            } else {
                $.ajax({
                    url:document.location.href + "?sessionId=" + sessionId + "&complete=true&cursorAt="
                        + editor.getCursor(true).line + "," + editor.getCursor(true).ch,
                    context:document.body,
                    success:startComplete,
                    dataType:"json",
                    type:"POST",
                    data:{text:i},
                    timeout:10000,
                    error:function () {
                        isCompletionInProgress = false;
                        setStatusBarMessage(REQUEST_ABORTED);
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
        startComplete(null);
        isContinueComplete = true;
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
            var i = 0;
            while (typeof data[i] != "undefined") {
                var lookupElement = new LookupElement(data[i].name, data[i].tail, data[i].icon)
                keywords.push(lookupElement);
                i++;
            }
        } else {
            if (!isContinueComplete) {
                keywords = [];
                return;
            }
            isContinueComplete = false;
        }
        var completions = getCompletions(token);
        if ((completions.length == 0) || (completions == null)) return;
        if (completions.length == 1) {
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
            if (is_chrome) {
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
        sel.firstChild.selected = true;
        sel.size = Math.min(10, i);

        var pos = editor.cursorCoords();
        complete.style.left = pos.x + "px";
        complete.style.top = pos.yBot + "px";
        document.body.appendChild(complete);
        document.getElementById("complete").focus();

        // Hack to hide the scrollbar.
        if (i <= 10)
            complete.style.width = (sel.clientWidth - 1) + "px";

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
                event.stop();
                pick();
            }
            // Escape
            else if (code == 27) {
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
        var found = [], start = token.string;

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

    String.prototype.hashCode = function () {
        var hash = 0;
        if (this.length == 0) return hash;
        for (i = 0; i < this.length; i++) {
            char = this.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash;
    }

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

});
