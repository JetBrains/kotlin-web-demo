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
 * Date: 3/29/12
 * Time: 1:56 PM
 */

var CompletionFromClient = (function () {

    function CompletionFromClient() {

        var instance = {
            getCompletion:function (configurationType, programText, cursorLine, cursorCh) {
                var confTypeString = Configuration.getStringFromType(configurationType);
                getCompletion(confTypeString, programText, cursorLine, cursorCh);
            },
            onLoadCompletion:function (data) {
            },
            onFail:function (exception) {
            }
        };

        var isLoadingCompletion = false;

        function getCompletion(confTypeString, programText, cursorLine, cursorCh) {
            if (!isLoadingCompletion) {
                getDataFromApplet(confTypeString, programText, cursorLine, cursorCh);
            }
        }

        var isFirstTryToLoadApplet = true;

        function getDataFromApplet(confTypeString, programText, cursorLine, cursorCh) {
            if (document.getElementById("myapplet") == null) {
                $("div#all").after("<applet id=\"myapplet\" code=\"org.jetbrains.webdemo.MainApplet\" width=\"0\" height=\"0\" ARCHIVE=\"/static/WebDemoApplet" + APPLET_VERSION + ".jar\" style=\"display: none;\"></applet>");
            }
            try {
                var dataFromApplet;
                try {
                    dataFromApplet = $("#myapplet")[0].getCompletion(programText, cursorLine, cursorCh, confTypeString);
                } catch (e) {
                    // For Chrome: wait until user accept work with applet
                    var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
                    if (is_chrome && e.indexOf("getCompletion") > 0 && isFirstTryToLoadApplet) {
                        isFirstTryToLoadApplet = false;
                        setTimeout(function () {
                            getDataFromApplet(programText, cursorLine, cursorCh, confTypeString);
                        }, 3000);
                        return;
                    } else {
                        instance.onFail(e);
                    }
                    return;
                }
                isLoadingCompletion = false;
                var data = eval(dataFromApplet);
                if (checkDataForNull(data)) {
                    if (checkDataForException(data)) {
                        instance.onLoadCompletion(data);
                    } else {
                        instance.onFail(data);
                    }
                } else {
                    instance.onFail("Incorrect data format.");
                }
            } catch (e) {
                isLoadingCompletion = false;
                instance.onFail(e);
            }
        }

        return instance;
    }


    return CompletionFromClient;
})();