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

package org.jetbrains.webdemo.server;

public class ApplicationSettings {
    public static String JAVA_HOME = null;
    public static String JAVA_EXECUTE = "";
    public static String WEBAPP_ROOT_DIRECTORY = "";
    public static String AUTH_REDIRECT = "kotlin-demo.jetbrains.com";
    public static String TIMEOUT_FOR_EXECUTION = "5000";
    public static String IS_TEST_VERSION = "false";

    public static String OUTPUT_DIRECTORY = "out";
    public static String EXAMPLES_DIRECTORY = "examples";
    public static String HELP_DIRECTORY = "help";
    public static String LOGS_DIRECTORY = "logs";
    public static String STATISTICS_DIRECTORY = "statistics";

    public static String HELP_FOR_EXAMPLES = "helpExamples.xml";
    public static String HELP_FOR_WORDS = "helpWords.xml";

    public static String KOTLIN_LIB = "";

    public static String KOTLIN_ERROR_MESSAGE = "Exception in Kotlin compiler: a bug was reported to developers.";
    public static String KOTLIN_VERSION = "0.4.108";

    public static String RT_JAR = "";

    public static final String DATABASE_VERSION = "1.0";

    private ApplicationSettings() {

    }


}
