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
 * Date: 4/2/12
 * Time: 6:44 PM
 * To change this template use File | Settings | File Templates.
 */


function Configuration(mode, dependencies, runner) {
    this.mode = mode;
    this.dependencies = dependencies;
    this.runner = runner;
}
Configuration.mode = {CLIENT:"client", SERVER:"server", ONRUN:"onrun"};
Configuration.dependencies = {JAVA:"java", JS:"js", CANVAS:"canvas"};
Configuration.runner = {JAVA:"java", JS:"js", CANVAS:"canvas"};

var ConfigurationComponent = (function () {

    var eventHandler = new EventsHandler();
    var configuration = new Configuration(null, Configuration.dependencies.JAVA, Configuration.runner.JAVA);

    function ConfigurationComponent() {

        var instance = {
            addListener: function (name, f) {
                eventHandler.addListener(name, f);
            },
            fire: function (name, param) {
                eventHandler.fire(name, param);
            },
            getConfiguration: function() {
                return configuration;
            },
            loadExampleOrProgram: function(status, example) {
                if (status) {
                    //TODO
                    configuration.dependencies = example.defaultDependencies;
                    configuration.runner = example.defaultDependencies;
                    $("#runConfigurationMode").selectmenu("value", example.defaultDependencies);
                }
            }
        };

        $("#dialogAboutRunConfiguration").dialog({
            modal:"true",
            width:300,
            autoOpen:false
        });

        $("#whatimg").click(function () {
            $("#dialog").dialog("open");
        });

        $("#whatimgRunConf").click(function () {
            $("#dialogAboutRunConfiguration").dialog("open");
        });

        $("#runConfigurationMode").selectmenu({
                change:function () {
                    configuration.dependencies = $("#runConfigurationMode").val();
                    configuration.runner = $("#runConfigurationMode").val();
                    fireChangeEvent();
                }
            }
        );

        $("#dialog").dialog({
            modal:"true",
            width:400,
            autoOpen:false
        });


        var isAppletLoaded = false;

        $(".applet-enable").click(function () {
            var parent = $(this).parents('.switch');
            $('.applet-disable', parent).removeClass('selected');
            $('.applet-disable', parent).removeClass('rightServer');
            $('.applet-disable', parent).addClass('leftServer');
            $('.applet-nohighlighting', parent).removeClass('selected');
            $(this).addClass('selected');
            $("#appletcheckbox").attr('checked', true);
            $("#nohighlightingcheckbox").attr('checked', false);
            saveModeToCookies("applet");
            configuration.mode = Configuration.mode.CLIENT;
            if (!isAppletLoaded) {
                waitLoadingApplet();
                try {
                    document.getElementById("myapplet").style.display = "block";
                    isAppletLoaded = true;
                    var title = $("#appletclient").attr("title");
                    var pos = title.indexOf("Your browser can't run Java Applets.");
                    if (pos != -1) {
                        title = title.substring(0, pos);
                        $("#appletclient").attr("title", title);
                    }
                } catch (e) {
                    eventHandler.fire("hide_loader");
                    $(".applet-disable").click();
                }
            }
        });

        function waitLoadingApplet() {
            if (document.getElementById("myapplet") == null) {
                $("div#all").after("<applet id=\"myapplet\" code=\"org.jetbrains.webdemo.MainApplet\" width=\"0\" height=\"0\" ARCHIVE=\"/static/WebDemoApplet14032012.jar\" style=\"display: none;\"></applet>");
            }
            var applet = $("#myapplet")[0];
            eventHandler.fire("show_loader");
            function performAppletCode(count) {
                if (!applet.checkApplet && count > 0) {
                    setTimeout(function () {
                        performAppletCode(count - 1);
                    }, 1000);
                }
                else if (applet.checkApplet) {
                    //eventHandler.fire("hide_loader");
                }
                else {
                    //eventHandler.fire("hide_loader");
                }
            }

            performAppletCode(10);
        }

        $(".applet-disable").click(function () {
            var parent = $(this).parents('.switch');
            $('.applet-enable', parent).removeClass('selected');
            $('.applet-nohighlighting', parent).removeClass('selected');
            $(this).addClass('selected');
            $("#appletcheckbox").attr('checked', false);
            $("#nohighlightingcheckbox").attr('checked', false);
            saveModeToCookies("server");
            configuration.mode = Configuration.mode.SERVER;
            //todo setStatusBarMessage("");
        });

        $(".applet-nohighlighting").click(function () {
            var parent = $(this).parents('.switch');
            $('.applet-disable', parent).removeClass('selected');
            $('.applet-disable', parent).removeClass('leftServer');
            $('.applet-disable', parent).addClass('rightServer');
            $('.applet-enable', parent).removeClass('selected');
            $(this).addClass('selected');
            $("#nohighlightingcheckbox").attr('checked', true);
            saveModeToCookies("nohighlighting");
            configuration.mode = Configuration.mode.ONRUN;
            //todo setStatusBarMessage("");
        });

        var mode = getModeFromCookies();
        if (mode == "applet") {
            configuration.mode = Configuration.mode.CLIENT;
            $(".applet-enable").click();
        } else if (mode == "server") {
            configuration.mode = Configuration.mode.SERVER;
            $(".applet-disable").click();
        } else {
            configuration.mode = Configuration.mode.ONRUN;
            $(".applet-nohighlighting").click();
        }

        //todo
        setTimeout(fireChangeEvent, 100);

        return instance;
    }

    function saveModeToCookies(mode) {
        $.cookie("typeCheckerMode", mode);
    }

    function getModeFromCookies() {
        return $.cookie("typeCheckerMode");
    }

    function fireChangeEvent() {
        if (configuration.mode != null && configuration.runner != null && configuration.dependencies != null) {
            eventHandler.fire("change_configuration", true, configuration);
        } else {
            eventHandler.fire("change_configuration", false, null);
        }
    }

    return ConfigurationComponent;
})();