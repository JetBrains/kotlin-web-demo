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

/**
 * Created with IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 3/28/12
 * Time: 6:40 PM
 */

var CompletionProvider = (function () {
    function CompletionProvider() {

        var instance = {
            getCompletion:function (data) {
                getCompletion(data[0], data[1], data[2], data[3], data[4]);
            },
            onLoadCompletion:function (data) {
            },
            onFail:function (error) {
            }
        };

        var isCompletionInProgress = false;

        function getCompletion(dependencies, mode, file, cursorLine, cursorCh) {
            //TODO runTimerForNonPrinting();
            if (mode == Configuration.mode.ONRUN.name) {
                isCompletionInProgress = false;
                instance.onLoadCompletion(COMPLETION_ISNOT_AVAILABLE);
                return;
            }
            if (!isCompletionInProgress) {
                isCompletionInProgress = true;
                if (mode == Configuration.mode.CLIENT.name) {
                    getCompletionFromApplet(dependencies, file, cursorLine, cursorCh);
                    isCompletionInProgress = false;
                } else {
                    $.ajax({
                        url:generateAjaxUrl("complete", cursorLine + "," + cursorCh + "&runConf=" + dependencies),
                        context:document.body,
                        success:function (data) {
                            isCompletionInProgress = false;
                            if (checkDataForNull(data)) {
                                if (checkDataForException(data)) {
                                    instance.onLoadCompletion(data);
                                } else {
                                    instance.onFail(data);
                                }
                            } else {
                                instance.onFail("Incorrect data format.");
                            }
                        },
                        dataType:"json",
                        type:"POST",
                        data:{text:file},
                        timeout:10000,
                        error:function (jqXHR, textStatus, errorThrown) {
                            isCompletionInProgress = false;
                            instance.onFail(textStatus + " : " + errorThrown);
                        }
                    });
                }
            }
        }

        var isFirstTryToLoadApplet = true;

        function getCompletionFromApplet(dependencies, file, cursorLine, cursorCh) {
            if (document.getElementById("myapplet") == null) {
                $("div#all").after("<applet id=\"myapplet\" code=\"org.jetbrains.webdemo.MainApplet\" width=\"0\" height=\"0\" ARCHIVE=\"/static/WebDemoApplet" + APPLET_VERSION + ".jar\" style=\"display: none;\"></applet>");
            }
            try {
                var dataFromApplet;
                try {
                    dataFromApplet = $("#myapplet")[0].getCompletion(file, cursorLine, cursorCh, dependencies);
                } catch (e) {
                    if (e.indexOf("getCompletion") > 0) {
                        var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
                        if (is_chrome && isFirstTryToLoadApplet) {
                            isFirstTryToLoadApplet = false;
                            setTimeout(function () {
                                getCompletionFromApplet(dependencies, file, cursorLine, cursorCh);
                            }, 3000);
                            return;
                        }
                        //TODO add tooltip for client mode
                        //$(".applet-nohighlighting").click();
                        //setStatusBarError(GET_FROM_APPLET_FAILED);

                        //var title = $("#appletclient").attr("title");
                        //if (title.indexOf(GET_FROM_APPLET_FAILED) == -1) {
                        //    $("#appletclient").attr("title", title + ". " + GET_FROM_APPLET_FAILED);
                        //}
                    } else {
                        instance.onFail(e);
                    }
                    return;
                }
                var data = eval(dataFromApplet);
                isCompletionInProgress = false;
                instance.onLoadCompletion(data);
            } catch (e) {
                isCompletionInProgress = false;
                instance.onFail(e);
            }
        }

        return instance;
    }


    return CompletionProvider;
})();