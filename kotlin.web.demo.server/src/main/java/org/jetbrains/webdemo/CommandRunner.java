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

package org.jetbrains.webdemo;


import java.io.File;

public class CommandRunner {

    public static void setServerSettingFromTomcatConfig(String setting, String value) {
        System.out.println("Loaded from config file: " + setting + " " + value);
        if (value.isEmpty()) {
            System.err.println("Empty value for setting: " + setting);
        } else if (setting.equals("app_output_dir")) {
            CommonSettings.LOGS_DIRECTORY = value + File.separator + "logs";
            System.setProperty("kotlin.web.demo.log4j", value);
        } else if (setting.equals("backend_url")) {
            ApplicationSettings.BACKEND_URL = value;
        }else if (setting.equals("is_test_version")) {
            CommonSettings.IS_TEST_VERSION = Boolean.parseBoolean(value);
        } else if (setting.equals("azure_key")) {
            ApplicationSettings.AZURE_OAUTH_CREDENTIALS.KEY = value;
        } else if (setting.equals("azure_secret")) {
            ApplicationSettings.AZURE_OAUTH_CREDENTIALS.SECRET = value;
        } else if (setting.equals("azure_tenant")) {
            ApplicationSettings.AZURE_OAUTH_CREDENTIALS.TENANT = value;
        } else if (setting.equals("google_key")) {
            ApplicationSettings.GOOGLE_OAUTH_CREDENTIALS.KEY = value;
        } else if (setting.equals("google_secret")) {
            ApplicationSettings.GOOGLE_OAUTH_CREDENTIALS.SECRET = value;
        } else if (setting.equals("twitter_key")) {
            ApplicationSettings.TWITTER_OAUTH_CREDENTIALS.KEY = value;
        } else if (setting.equals("twitter_secret")) {
            ApplicationSettings.TWITTER_OAUTH_CREDENTIALS.SECRET = value;
        } else if (setting.equals("facebook_key")) {
            ApplicationSettings.FACEBOOK_OAUTH_CREDENTIALS.KEY = value;
        } else if (setting.equals("facebook_secret")) {
            ApplicationSettings.FACEBOOK_OAUTH_CREDENTIALS.SECRET = value;
        } else if (setting.equals("github_key")) {
            ApplicationSettings.GITHUB_OAUTH_CREDENTIALS.KEY = value;
        } else if (setting.equals("github_secret")) {
            ApplicationSettings.GITHUB_OAUTH_CREDENTIALS.SECRET = value;
        } else if (setting.equals("jba_secret")) {
            ApplicationSettings.JET_ACCOUNT_CREDENTIALS.SECRET = value;
        } else {
            System.err.println("Incorrect setting in config.properties file: " + setting);
        }

    }
}
