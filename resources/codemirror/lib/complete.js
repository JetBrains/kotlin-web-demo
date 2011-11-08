(function () {

    var goToSymbolShortcutKeys = [17, 32];

    var isCompletionInProgress = false;

    var timer;
    var timerIntervalForNonPrinting = 300;

    /*$(document).keydown(function(event) {
     if (event.code != 38 && event.code != 40) {
     runTimerForNonPrinting();
     }
     });*/

    function runTimerForNonPrinting() {
        if (timer) {
            clearTimeout(timer);
            timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
        }
        else {
            timer = setTimeout(getHighlighting, timerIntervalForNonPrinting);
        }
    }

    function getHighlighting() {
        if (!isCompletionInProgress) {
            getErrors();
        }
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

    var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers:true,
        matchBrackets:true,
        mode:"text/kotlin",
        onKeyEvent:function (i, event) {
            // Hook into ctrl-space
            if (isGotoKeysPressed(event, goToSymbolShortcutKeys)) {
                event.stop();
                return beforeComplete();
            }

        },
        onChange: runTimerForNonPrinting,
        onCursorActivity: updateStatusBar
    });

    function updateStatusBar() {
        var lineNumber = editor.getCursor(true).line;
        var text = editor.lineInfo(lineNumber).markerText;
        if (text != null) {
            text = text.substring(text.indexOf("title=\"") + 7);
            text = text.substring(0, text.indexOf("\""));
            document.getElementById("statusBar").innerHTML = "line " + lineNumber + " - " + text;
        } else {
            document.getElementById("statusBar").innerHTML = "";
        }
    }

    /*var editorConsole = CodeMirror.fromTextArea(document.getElementById("console"), {
     lineNumbers: true,
     onKeyEvent: function(i, event) {
     // Hook into ctrl-space
     if (event.ctrlKey) {
     event.stop();
     return consoleInput();
     }

     }
     });

     function consoleInput() {
     var i = editorConsole.getValue();
     $.ajax({
     url: document.location.href + "?consoleInput=true",
     context: document.body,
     success: onCompileSuccess,
     dataType: "html",
     type: "POST",
     data: {text: i},
     //timeout: 30000,
     error: function() {
     document.getElementById("compilationResult").innerHTML = "Your request is aborted. Impossible to get data from server. " + error;
     }
     });
     }
     */
    function isGotoKeysPressed(event, array) {
        var args = args || {};

        for (var i = 0; i < array.length; ++i) {
            args[i] = array[i];
            if ((event.ctrlKey) && (args[i] == 17)) {
                args[i] = true;
            }
            if (args[i] == event.keyCode) {
                args[i] = true;
            }
        }
        for (var k = 0; k < array.length; ++k) {
            if (args[k] != true) {
                return false;
            }
        }
        return true;
    }

    //editor.markText({line: 0, ch: 0}, {line: 0, ch: 5}, "newLine");

    var compilationInProgress = false;

    $("#arguments").val("");

    function runOrCompile(param, text, error) {
        compilationInProgress = true;
        var arguments = $("#arguments").val();
        var i = editor.getValue();
        $.ajax({
            url:document.location.href + "?" + param + "=true",
            context:document.body,
            success:onCompileSuccess,
            dataType:"json",
            type:"POST",
            data:{text:i, consoleArgs:arguments},
            //timeout: 30000,
            error:function () {
                document.getElementById("compilationResult").innerHTML = "Your request is aborted. Impossible to get data from server. " + error;
            }
        });

        document.getElementById("compilationResult").innerHTML = text;

    }

    $("#compile").click(function () {
        runOrCompile("compile", "Compilation in progress...", "Compilation failed.");
    });

    $("#run").click(function () {
        runOrCompile("run", "Run project...", "Run action failed.");
    });

    $("#markAll").click(function () {
        var lineCount = editor.lineCount();
        var lastLine = editor.getLine(lineCount - 1);
        var end = lastLine.length;
        editor.setSelection(eval('(' + "{line: 0, ch: 0}" + ')'), eval('(' + "{line:" + lineCount + ", ch: " + end + "}" + ')'));
        //window.clipboardData.setData("Text", editor.getValue());
    });

    var array = {};
    var arrayLinesMarkers = {};


    function onCompileSuccess(data) {
        if (data != null) {
            var i = 0;
            document.getElementById("compilationResult").innerHTML = "";
            var errors = document.createElement("div");
            while (typeof data[i] != "undefined") {
                //If there is a compilation error
                if (typeof data[i].message != "undefined") {
                    alert("a")
                    var p = document.createElement("p");
                    var image = document.createElement("img");
                    if (data[i].type == "ERROR") {
                        p.className = "newLineError";
                        image.src = "icons/error.png";
                    } else if (data[i].type == "WARNING") {
                        p.className = "newLineWarning";
                        image.src = "icons/warning.png";
                    }
                    p.appendChild(image);
                    var text = document.createElement("span");
                    text.innerHTML = data[i].message;
                    p.appendChild(text);
                    errors.appendChild(p);
                } else {
                    var p = document.createElement("p");
                    if (data[i].type == "err") {
                        p.className = "newLineError";
                    }
                    p.innerHTML = data[i].text;
                    errors.appendChild(p);
                }
                i++;
            }
            document.getElementById("compilationResult").appendChild(errors);
        }
        compilationInProgress = false;
    }

    function removeStyles() {
        var i = 0;
        while (typeof array[i] != "undefined") {
            array[i].clear();
            i++;
        }
        i = 0;
        while (typeof arrayLinesMarkers[i] != "undefined") {
            editor.clearMarker(arrayLinesMarkers[i]);
            i++;
        }
    }


    var now;
    var hashCode;
    var isLoadingHighlighting = false;

    function getErrors() {
        if ((!compilationInProgress) && (!isLoadingHighlighting)) {
            isLoadingHighlighting = true;
            now = new Date().getTime();
            document.getElementById("compilationResult0").innerHTML = "begin " + (new Date().getTime() - now);
            var i = editor.getValue();
            hashCode = editor.getValue().hashCode();
            $.ajax({
                //url: document.location.href + "?sendData=true&" + new Date().getTime() + "&lineNumber=" + lineNumber,
                url:document.location.href + "?sendData=true",
                context:document.body,
                success:onAjaxSuccess,
                dataType:"json",
                type:"POST",
                data:{text:i},
                //timeout: 10000,
                error:function () {
                    isLoadingHighlighting = false;
                }
            });
        }
    }


    function onAjaxSuccess(data) {
        document.getElementById("compilationResult0").innerHTML = "onSuccess " + (new Date().getTime() - now);
        if (data != null) {
            var i = 0;
            removeStyles();
            document.getElementById("compilationResult1").innerHTML = "remove " + (new Date().getTime() - now);
            while (typeof data[i] != "undefined") {
                array[i] = editor.markText(eval('(' + data[i].x + ')'), eval('(' + data[i].y + ')'), data[i].className, "ddd");
                var title = data[i].titleName;
                var start = eval('(' + data[i].x + ')');

                if ((data[i].severity == 'WARNING') || (data[i].severity == 'TYPO')) {
                    document.getElementById("compilationResult1").innerHTML = document.getElementById("compilationResult1").innerHTML + editor.lineInfo(start.line).markerText;
                    //editor.setMarker(start.line, '<img src="/icons/warning.png" title="' + title + '"/>%N%');
                    editor.setMarker(start.line, '<span class=\"warningGutter\" title="' + title + '">  </span>%N%');
                } else {
                    editor.setMarker(start.line, '<span class=\"errorGutter\" title="' + title + '">  </span>%N%');
                }
                arrayLinesMarkers[i] = start.line;
                var el = document.getElementById(start.line + " " + start.ch);
                if (el != null) {
                    alert("aaa");
                    el.setAttribute("title", title);
                }

                i++;
            }
            document.getElementById("compilationResult2").innerHTML = "after all " + (new Date().getTime() - now);
        }
        updateStatusBar();
        isLoadingHighlighting = false;
    }

    function beforeComplete() {
        runTimerForNonPrinting();
        if (!isCompletionInProgress) {
            isCompletionInProgress = true;
            var i = editor.getValue();
            $.ajax({
                //url: document.location.href + "?sendData=true&" + new Date().getTime() + "&lineNumber=" + lineNumber,
                url:document.location.href + "?complete=true&cursorAt=" + editor.getCursor(true).line + "," + editor.getCursor(true).ch,
                context:document.body,
                success:startComplete,
                dataType:"json",
                type:"POST",
                data:{text:i}//,
                //    timeout: 10000
            });
            //}   else {
            //     isCompletionInProgress = true;
        }
    }

    function LookupElement(name, tail, icon) {
        this.icon = icon;
        this.name = name;
        this.tail = tail;
    }

    var keywords;
    var isContinueComplete = false;

    function continueComplete() {
        startComplete(null);
        isContinueComplete = true;
    }

    function startComplete(data) {
        //ideaKeywords = (data[0].content).split(" ");
        // We want a single cursor position.
        if (editor.somethingSelected()) return;
        // Find the token at the cursor
        var cur = editor.getCursor(), token = editor.getTokenAt(cur);
        if (data != null) {
            keywords = [];
            var i = 0;
            while (typeof data[i] != "undefined") {
                var lookupElement = new LookupElement(data[i].name, data[i].tail, data[i].icon)
                keywords.push(lookupElement);
                i++;
            }
        } else {
            if (!isContinueComplete) {
                keywords = [];
            }
            isContinueComplete = false;
        }
        var completions = getCompletions(token);
        isCompletionInProgress = false;
        if ((completions.length == 0) || (completions == null)) return;
        if (completions.length == 1) {
            insert(completions[0].name);
            return true;
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
                if ((token.string == '.') || (token.string == ' ') || (token.string == '(')) {
                    editor.replaceRange(str, {line:cur.line, ch:token.end}, {line:cur.line, ch:token.end});
                } else {
                    editor.replaceRange(str, {line:cur.line, ch:token.start}, {line:cur.line, ch:token.end});
                }
            } else {
                alert("Exception");
            }
        }

        // Build the select widget
        var complete = document.createElement("div");
        complete.className = "completions";
        var sel = complete.appendChild(document.createElement("select"));

        for (i = 0; i < completions.length; ++i) {
            var opt = sel.appendChild(document.createElement("option"));
            var pEl = document.createElement("p");
            pEl.className = "lookupElement";

            var icon = document.createElement("img");
            icon.className = "lookupElementIcon";
            icon.src = completions[i].icon;
            pEl.appendChild(icon);
            //opt.appendChild(document.createTextNode(completions[i].name));
            var spanName = document.createElement("span");
            spanName.className = "lookupElementName";
            spanName.innerHTML = completions[i].name;
            pEl.appendChild(spanName);

            var spanTail = document.createElement("span");
            spanTail.className = "lookupElementTail";
            spanTail.innerHTML = completions[i].tail;
            pEl.appendChild(spanTail);
            document.getElementById("compilationResult0").innerHTML = document.getElementById("compilationResult0").innerHTML + completions[i].name;
            //opt.appendChild(document.createTextNode(completions[i].tail));
            opt.appendChild(pEl);
        }

        /*i = 0;
         while (typeof data[i] != "undefined") {
         var opt = sel.appendChild(document.createElement("option"));
         var image = document.createElement("img");
         image.src = data[i].icon;
         opt.appendChild(image);
         opt.appendChild(document.createTextNode(data[i].name));
         opt.appendChild(document.createTextNode(data[i].tail));

         i++;
         }*/
        //alert(completions.length + " " + data.length);

        sel.multiple = true;
        sel.firstChild.selected = true;
        sel.size = Math.min(10, i);
        var pos = editor.cursorCoords();
        complete.style.left = pos.x + "px";
        complete.style.top = pos.yBot + "px";
        document.body.appendChild(complete);
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
            //insert(sel.options[sel.selectedIndex].text);
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
            }
            else if (code != 38 && code != 40) {
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
        return true;
    }


    function getCompletions(token) {
        var found = [], start = token.string;
        // alert(start);
        function maybeAdd(lookupElement) {
            //alert(str);
            if (lookupElement.name.indexOf(start) == 0) found.push(lookupElement);
        }

        function add(str) {
            found.push(str);
        }

        //alert("!" + start + "!");
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

})();
