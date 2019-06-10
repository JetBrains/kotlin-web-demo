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

public class ApplicationSettings {
    public static final String DATABASE_VERSION = "1.0";
    public static String BACKEND_URL = "";
    public static String EXAMPLES_DIRECTORY = "examples";
    public static String HELP_FOR_WORDS = "helpWords.xml";
    public static boolean LOAD_TEST_VERSION_OF_EXAMPLES = false;
    public static OauthCredentials GITHUB_OAUTH_CREDENTIALS = new OauthCredentials();
    public static OauthCredentials GOOGLE_OAUTH_CREDENTIALS = new OauthCredentials();
    public static OauthCredentials FACEBOOK_OAUTH_CREDENTIALS = new OauthCredentials();
    public static OauthCredentials TWITTER_OAUTH_CREDENTIALS = new OauthCredentials();
    public static OauthCredentials JET_ACCOUNT_CREDENTIALS = new OauthCredentials();
    public static AzureCredentials AZURE_OAUTH_CREDENTIALS = new AzureCredentials();


    private ApplicationSettings() {

    }

    public static class OauthCredentials {
        public String KEY = "";
        public String SECRET = "";
    }

    public static class AzureCredentials extends OauthCredentials {
        public String TENANT = "";
    }

}
