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
 */


function Configuration(mode, type) {
    this.mode = mode;
    this.type = type;
}

function ConfigurationType(runner, dependencies) {
    this.runner = runner;
    this.dependencies = dependencies;
}

ConfigurationType.runner = {JAVA:"java", JS:"js"};
ConfigurationType.dependencies = {STANDARD:"standard", CANVAS:"canvas"};

function ConfigurationMode(name, highlighter, completer) {
    this.name = name;
    this.highlighter = highlighter;
    this.completer = completer;
}

ConfigurationType.runner = {JAVA:"java", JS:"js"};
ConfigurationType.dependencies = {STANDARD:"standard", CANVAS:"canvas"};

Configuration.mode = {CLIENT:new ConfigurationMode("client", new HighlightingFromClient(), new CompletionFromClient()),
    SERVER:new ConfigurationMode("server", new HighlightingFromServer(), new CompletionFromServer()),
    ONRUN:new ConfigurationMode("onrun", new HighlightingFromServer(), new CompletionOnRun())};

Configuration.type = {JAVA:new ConfigurationType(ConfigurationType.runner.JAVA, ConfigurationType.dependencies.STANDARD),
    JS:new ConfigurationType(ConfigurationType.runner.JS, ConfigurationType.dependencies.STANDARD),
    CANVAS:new ConfigurationType(ConfigurationType.runner.JS, ConfigurationType.dependencies.CANVAS)};

var ConfigurationComponent = (function () {

    var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.type.JAVA);

    function ConfigurationComponent() {

        var instance = {
            getConfiguration:function () {
                return configuration;
            },
            // type: String
            updateConfiguration:function (type) {
                configuration = new Configuration(configuration.mode,  Configuration.getTypeFromString(type));
                $("#runConfigurationMode").selectmenu("value", type);
                fireChangeEvent();
            },
            onChange:function (configuration) {
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
                    configuration = new Configuration(configuration.mode, Configuration.getTypeFromString($("#runConfigurationMode").val()));
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
            configuration = new Configuration(Configuration.mode.CLIENT, configuration.type);
            fireChangeEvent();
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
                    //TODO eventHandler.fire("hide_loader");
                    $(".applet-disable").click();
                }
            }
        });

        function waitLoadingApplet() {
            if (document.getElementById("myapplet") == null) {
                $("div#all").after("<applet id=\"myapplet\" code=\"org.jetbrains.webdemo.MainApplet\" width=\"0\" height=\"0\" ARCHIVE=\"/static/WebDemoApplet" + APPLET_VERSION + ".jar\" style=\"display: none;\"></applet>");
            }
            var applet = $("#myapplet")[0];
            //TODO eventHandler.fire("show_loader");
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
            configuration = new Configuration(Configuration.mode.SERVER, configuration.type);
            fireChangeEvent();
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
            configuration = new Configuration(Configuration.mode.ONRUN, configuration.type);
            fireChangeEvent();
        });

        var mode = getModeFromCookies();
        if (mode == "applet") {
            $(".applet-enable").click();
        } else if (mode == "server") {
            $(".applet-disable").click();
        } else {
            $(".applet-nohighlighting").click();
        }

        function saveModeToCookies(mode) {
            $.cookie("typeCheckerMode", mode);
        }

        function getModeFromCookies() {
            return $.cookie("typeCheckerMode");
        }

        function fireChangeEvent() {
            if (configuration.mode.name != null && configuration.type.runner != null && configuration.type.dependencies != null) {
                instance.onChange(configuration);
            } else {
                instance.onFail("Incorrect format for configuration");
            }
        }

        return instance;
    }

    Configuration.getStringFromType = function (type) {
        if (type == Configuration.type.JS) {
            return "js";
        } else if (type == Configuration.type.CANVAS) {
            return "canvas";
        } else {
            return "java";
        }
    };

    Configuration.getTypeFromString = function(type) {
        if (type == "js") {
            return Configuration.type.JS;
        } else if (type == "canvas") {
            return Configuration.type.CANVAS;
        } else {
            return Configuration.type.JAVA;
        }
    };

    return ConfigurationComponent;
})();