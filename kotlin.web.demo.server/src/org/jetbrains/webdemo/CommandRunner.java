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

package org.jetbrains.webdemo;

import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.File;

public class CommandRunner {

    public static void setServerSettingFromTomcatConfig(String setting, String value) {
        ErrorWriter.writeInfoToConsole("Loaded from config file: " + setting + " " + value);
        if (value.isEmpty()) {

        } else if (setting.equals("java_home")) {
            ApplicationSettings.JAVA_HOME = value;
        } else if (setting.equals("java_execute")) {
            ApplicationSettings.JAVA_EXECUTE = value;
        } else if (setting.equals("timeout")) {
            ApplicationSettings.TIMEOUT_FOR_EXECUTION = Integer.parseInt(value);
        } else if (setting.equals("app_home")) {
            ApplicationSettings.OUTPUT_DIRECTORY = value + File.separator + "out";
            ApplicationSettings.STATISTICS_DIRECTORY = value + File.separator + "statistics";
            ApplicationSettings.LOGS_DIRECTORY = value + File.separator + "logs";
            ApplicationSettings.EXAMPLES_DIRECTORY = value + File.separator + "examples";
            ApplicationSettings.HELP_DIRECTORY = value + File.separator + "help";
            System.setProperty("kotlin.web.demo.log4j", value);
        } else if (setting.equals("auth_redirect")) {
            ApplicationSettings.AUTH_REDIRECT = value;
        } else if (setting.equals("is_test_version")) {
            ApplicationSettings.IS_TEST_VERSION = value;
        } else {
            ErrorWriter.writeErrorToConsole("Incorrect setting in config.properties file: " + setting);
        }

    }
}
