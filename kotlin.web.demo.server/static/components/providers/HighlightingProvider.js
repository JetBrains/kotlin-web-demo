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
 * To change this template use File | Settings | File Templates.
 */

/* EVENTS:
 get_highlighting
 write_exception
 */

var HighlightingProvider = (function () {

    var eventHandler = new EventsHandler();

    function HighlightingProvider() {

        var instance = {
            addListener:function (name, f) {
                eventHandler.addListener(name, f);
            },
            fire:function (name, param) {
                eventHandler.fire(name, param);
            },
            getHighlighting:function (param) {
                getHighlighting(param[0], param[1], param[2]);
            }
        };

        return instance;
    }

    var isLoadingHighlighting = false;

    function getHighlighting(dependencies, mode, file) {
        if (!isLoadingHighlighting) {
            if (mode == Configuration.mode.ONRUN) {
                return;
            }
            isLoadingHighlighting = true;
            if (mode == Configuration.mode.CLIENT) {
                getDataFromApplet(dependencies, file);
                isLoadingHighlighting = false;
            } else {
                $.ajax({
                    url:RequestGenerator.generateAjaxUrl("highlight", dependencies),
                    context:document.body,
                    success:function (data) {
                        isLoadingHighlighting = false;
                        eventHandler.fire("get_highlighting", true, data);
                    },
                    dataType:"json",
                    type:"POST",
                    data:{text:file},
                    timeout:10000,
                    error:function () {
                        isLoadingHighlighting = false;
                        eventHandler.fire("get_highlighting", false, null);
                    }
                });
            }
        }
    }

    var isFirstTryToLoadApplet = true;

    function getDataFromApplet(dependencies, file) {
        if (document.getElementById("myapplet") == null) {
            $("div#all").after("<applet id=\"myapplet\" code=\"org.jetbrains.webdemo.MainApplet\" width=\"0\" height=\"0\" ARCHIVE=\"/static/WebDemoApplet14032012.jar\" style=\"display: none;\"></applet>");
        }
        try {
            var dataFromApplet;
            try {
                dataFromApplet = $("#myapplet")[0].getHighlighting(file, dependencies);
            } catch (e) {
                if (e.indexOf("getHighlighting") > 0) {
                    var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
                    if (is_chrome && isFirstTryToLoadApplet) {
                        isFirstTryToLoadApplet = false;
                        setTimeout(function () {
                            getDataFromApplet(dependencies, file);
                        }, 3000);
                        return;
                    }
                    //TODO set tooltip for applet mode
                    /*$(".applet-nohighlighting").click();
                     setStatusBarError(GET_FROM_APPLET_FAILED);

                     var title = $("#appletclient").attr("title");
                     if (title.indexOf(GET_FROM_APPLET_FAILED) == -1) {
                     $("#appletclient").attr("title", title + ". " + GET_FROM_APPLET_FAILED);
                     }*/
                } else {
                    eventHandler.fire("get_highlighting", false, null);
                }

                return;
            }
            var data = eval(dataFromApplet);
            if (typeof data != "undefined") {
                if ((typeof data[0] != "undefined") && (typeof data[0].exception != "undefined")) {
                    eventHandler.fire("get_highlighting", false, null);
                    eventHandler.fire("write_exception", false, data);
                } else {
                    isLoadingHighlighting = false;
                    eventHandler.fire("get_highlighting", true, data);
                }
            }
        } catch (e) {
            isLoadingHighlighting = false;
            eventHandler.fire("get_highlighting", false, e);
        }
    }

    return HighlightingProvider;
})();