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

function ConfigurationMode(name) {
    this.name = name;
}

ConfigurationType.runner = {JAVA: "java", JS: "js"};
ConfigurationType.dependencies = {STANDARD: "standard", CANVAS: "canvas", JUNIT: "junit"};

Configuration.mode = { SERVER: new ConfigurationMode("server"),
    ONRUN: new ConfigurationMode("onrun")};

Configuration.type = {JAVA: new ConfigurationType(ConfigurationType.runner.JAVA, ConfigurationType.dependencies.STANDARD),
    JS: new ConfigurationType(ConfigurationType.runner.JS, ConfigurationType.dependencies.STANDARD),
    CANVAS: new ConfigurationType(ConfigurationType.runner.JS, ConfigurationType.dependencies.CANVAS),
    JUNIT: new ConfigurationType(ConfigurationType.runner.JAVA, ConfigurationType.dependencies.JUNIT)};

var ConfigurationComponent = (function () {

    var configuration = new Configuration(Configuration.mode.ONRUN, Configuration.type.JAVA);

    function ConfigurationComponent() {

        var instance = {
            getConfiguration: function () {
                return configuration;
            },
            getType: function () {
                return Configuration.getStringFromType(configuration.type);
            },
            // type: String
            updateConfiguration: function (type) {
                configuration = new Configuration(configuration.mode, Configuration.getTypeFromString(type));
                $("#run-mode").val(type).selectmenu("refresh");
                if ($("#run-mode").val() == "java") {
                    document.getElementById("generated-code-link").innerHTML = "Generated classfiles";
                } else {
                    document.getElementById("generated-code-link").innerHTML = "Generated JavaScript code";
                }
                fireChangeEvent();
            },
            onChange: function (configuration) {
            }
        };

        $("#run-mode").on("selectmenuchange", function () {
            if ($("#run-mode").val() == "java") {
                document.getElementById("generated-code-link").innerHTML = "Generated classfiles";
            } else {
                document.getElementById("generated-code-link").innerHTML = "Generated JavaScript code";
            }
            configuration = new Configuration(configuration.mode, Configuration.getTypeFromString($("#run-mode").val()));
            fireChangeEvent();
        });


        function saveModeToCookies(mode) {
            $.cookie("typeCheckerMode", mode);
        }

        (function getModeFromCookies() {
            mode = $.cookie("typeCheckerMode");
            if (mode == "on-the-fly") {
                document.getElementById("on-the-fly-checkbox").checked = true;
                configuration = new Configuration(Configuration.mode.SERVER, configuration.type);
            } else {
                document.getElementById("on-the-fly-checkbox").checked = false;
                configuration = new Configuration(Configuration.mode.ONRUN, configuration.type);
            }
            fireChangeEvent();
        })();

        $(document).on("change", "#on-the-fly-checkbox", function () {
            var checkbox = document.getElementById("on-the-fly-checkbox");
            if (checkbox.checked == true) {
                configuration = new Configuration(Configuration.mode.SERVER, configuration.type);
                saveModeToCookies("on-the-fly")
            } else {
                configuration = new Configuration(Configuration.mode.ONRUN, configuration.type)
                saveModeToCookies("on-run")
            }
            fireChangeEvent();
        });

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
        } else if(type == Configuration.type.JUNIT){
            return "junit";
        } else if(type == Configuration.type.JAVA){
            return "java";
        }
    };

    Configuration.getTypeFromString = function (type) {
        if (type == "js") {
            return Configuration.type.JS;
        } else if (type == "canvas") {
            return Configuration.type.CANVAS;
        } else if( type == "java"){
            return Configuration.type.JAVA;
        } else if(type == "junit"){
            return Configuration.type.JUNIT;
        }
    };

    return ConfigurationComponent;
})();