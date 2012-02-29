/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 2/2/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */

var Highlighting = (function () {
    function Highlighting() {
        var instance = {

        };

        return instance;
    }

    var lastSelectedExample = 0;

    var isLoadingHighlighting = false;

    Highlighting.getErrors = function (f) {
        if (!isLoadingHighlighting) {
            if (runConfiguration.mode == "") {
                setTimeout(function () {
                    getErrors(f);
                }, 500);
            } else {
                getErrorsWithMode(f);
            }
        }
    };

    Highlighting.sendHighlightingRequest = function (onLoad) {
        isLoadingHighlighting = true;
        var i = replaceAll(editor.getValue(), "%", "@percent@");
        $.ajax({
            url:generateAjaxUrl("highlight", runConfiguration.mode),
            context:document.body,
            success:onLoad,
            dataType:"json",
            type:"POST",
            data:{text:i},
            timeout:10000,
            error:function () {
                clearProblemView();
                isLoadingHighlighting = false;
                setConsoleMessage(HIGHLIGHT_REQUEST_ABORTED);
            }
        });
    };

    Highlighting.onHighlightingSuccess = function (data) {
        isLoadingHighlighting = false;
        if (data == null) {
            return;
        }
        removeStyles();
        arrayClasses = [];
        arrayLinesMarkers = [];

        if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
            $("#tabs").tabs("select", 0);
            clearProblemView();
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
                if (i > 0) {
                    $("#tabs").tabs("select", 0);
                }
                $("#problems").html(problems.innerHTML);
                updateStatusBar();
                return;
            }
            arrayClasses.push(editor.markText(eval('(' + data[i].x + ')'), eval('(' + data[i].y + ')'), data[i].className));

            var title = unEscapeString(data[i].titleName);
            var start = eval('(' + data[i].x + ')');
            var severity = data[i].severity;

            if ((editor.lineInfo(start.line) != null) && (editor.lineInfo(start.line).markerText == null) || (editor.lineInfo(start.line) == null)) {
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


    Highlighting.getDataFromApplet = function (type) {
        var i = editor.getValue();
        try {
            var dataFromApplet;
            try {
                if (type == "complete") {
                    dataFromApplet = $("#myapplet")[0].getCompletion(i, editor.getCursor(true).line, editor.getCursor(true).ch, runConfiguration.mode);
                } else {
                    dataFromApplet = $("#myapplet")[0].getHighlighting(i, runConfiguration.mode);
                }
            } catch (e) {
//                alert(e);
                if ((e.indexOf("getHighlighting") > 0) || e.indexOf("getCompletion") > 0) {
                    var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
                    if (is_chrome && isFirstTryToLoadApplet) {
                        isFirstTryToLoadApplet = false;
                        setTimeout(function () {
                            getDataFromApplet(type);
                        }, 3000);
                        return;
                    }
                    $(".applet-nohighlighting").click();
                    setStatusBarError(GET_FROM_APPLET_FAILED);

                    var title = $("#appletclient").attr("title");
                    if (title.indexOf(GET_FROM_APPLET_FAILED) == -1) {
                        $("#appletclient").attr("title", title + ". " + GET_FROM_APPLET_FAILED);
                    }
                } else {
                    if (type == "complete") {
                        setStatusBarError(COMPLETE_REQUEST_ABORTED);
                    } else {
                        setStatusBarError(HIGHLIGHT_REQUEST_ABORTED);
                    }

                }

                return;
            }
            var data = eval(dataFromApplet);
            if (typeof data != "undefined") {
                if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
                    $("#tabs").tabs("select", 0);
                    clearProblemView();
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
                        Highlighting.onHighlightingSuccess(data);
                    }
                }
            }
        } catch (e) {
            isLoadingHighlighting = false;
            isCompletionInProgress = false;
        }
    };

    Highlighting.isLoadingErrors = function () {
        return isLoadingHighlighting;
    };

    Highlighting.setLoadingErrors = function (state) {
        isLoadingHighlighting = state;
    };

    function getErrorsWithMode(f) {
        if ($("#nohighlightingcheckbox").attr('checked') == 'checked') {
            return;
        }
        isLoadingHighlighting = true;
        if (isApplet) {
            getDataFromApplet("highlighting");
            isLoadingHighlighting = false;
        } else {
            setRunConfigurationMode();
            var i = editor.getValue();
            $.ajax({
                url:generateAjaxUrl("highlight", runConfiguration.mode),
                context:document.body,
                success:f,
                dataType:"json",
                type:"POST",
                data:{text:i},
                timeout:10000,
                error:function () {
                    clearProblemView();
                    isLoadingHighlighting = false;
                    setConsoleMessage(HIGHLIGHT_REQUEST_ABORTED);
                }
            });
        }
    }

    var counterSetConfMode = 0;


    return Highlighting;
})();