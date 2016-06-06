/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package org.jetbrains.webdemo.backend;

import org.jetbrains.webdemo.CommonSettings;

import java.io.File;

public class CommandRunner {

    public static void setServerSettingFromTomcatConfig(String setting, String value) {
        System.out.println("Loaded from config file: " + setting + " " + value);
        if (value.isEmpty()) {
            System.err.println("Empty value for setting: " + setting);
        } else if (setting.equals("java_home")) {
            BackendSettings.JAVA_HOME = value;
        } else if (setting.equals("java_execute")) {
            BackendSettings.JAVA_EXECUTE = value;
        } else if (setting.equals("timeout")) {
            BackendSettings.TIMEOUT_FOR_EXECUTION = Integer.parseInt(value);
        } else if (setting.equals("app_output_dir")) {
            BackendSettings.OUTPUT_DIRECTORY = value + File.separator + "out";
            CommonSettings.LOGS_DIRECTORY = value + File.separator + "logs";
            System.setProperty("kotlin.web.demo.log4j", value);
        } else if (setting.equals("is_test_version")) {
            CommonSettings.IS_TEST_VERSION = Boolean.parseBoolean(value);
        } else {
            System.err.println("Incorrect setting in config.properties file: " + setting);
        }

    }
}
