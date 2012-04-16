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
 * Date: 3/30/12
 * Time: 3:37 PM
 */


var MainPageView = (function () {

    var currentHeight;

    var this_instance = new MainPageView();

    var IE_SUPPORT = "Sorry, Internet Explorer under 8 version is currently unsupported.";

    function MainPageView() {

        var instance = {
            resize:function () {
                var isIE = navigator.appVersion.indexOf("MSIE") != -1;

                if (!isIE) {
                    var wheight = (window.innerHeight) ? window.innerHeight :
                        ((document.all) ? document.body.offsetHeight : null);
                    var wwidth = (window.innerWidth) ? window.innerWidth :
                        ((document.all) ? document.body.offsetWidth : null);
                    reassignSize(wwidth, wheight);
                } else {
                    //for avoid looping on resize in IE
                    if (typeof document != 'undefined' && typeof document.documentElement != 'undefined' && currentHeight != document.documentElement.clientHeight) {
                        reassignSize(document.documentElement.clientWidth, document.documentElement.clientHeight);
                        currentHeight = document.documentElement.clientHeight;
                    }
                }
            },
            setKotlinVersion:function () {
                $("#kotlinVersionTop").html("(" + KOTLIN_VERSION + ")");
                $("#kotlinVersion").html(WEB_DEMO_VERSION);
            },
            checkIEVersion:function () {
                if (navigator.appVersion.indexOf("MSIE") != -1) {
                    function getIEVersionNumber() {
                        var ua = navigator.userAgent;
                        var MSIEOffset = ua.indexOf("MSIE ");
                        if (MSIEOffset == -1) {
                            return 0;
                        } else {
                            return parseFloat(ua.substring(MSIEOffset + 5, ua.indexOf(";", MSIEOffset)));
                        }
                    }

                    if (getIEVersionNumber() < 8) {
                        document.getElementsByTagName("body")[0].innerHTML = IE_SUPPORT;
                        return true;
                    }
                    return false;
                }
            }

        };


        return instance;
    }

    MainPageView.resize = function () {
        this_instance.resize();
    };

    MainPageView.changeView = function () {
        if (!this_instance.checkIEVersion()) {
            this_instance.resize();
            this_instance.setKotlinVersion();
        }
    };

    function reassignSize(wwidth, wheight) {
        $("#scroll").css("height", (wheight - 262 - 72 - 20 - 10) + "px");
        $("#left").css("minHeight", (wheight - 72 - 20 - 10) + "px");
        $("#right").css("minHeight", (wheight - 72 - 20 - 10) + "px");
        $("#center").css("minHeight", (wheight - 72 - 20 - 10) + "px");
        $("#center").css("width", (wwidth - 292 - 292 - 2 - 20 - 5) + "px");
    }

    return MainPageView;
})();