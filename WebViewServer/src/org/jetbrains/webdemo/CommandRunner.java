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

import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.server.KotlinHttpServer;
import org.jetbrains.webdemo.server.ServerSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/14/11
 * Time: 11:54 AM
 */

public class CommandRunner {
//    private static final Logger LOG = Logger.getLogger(CommandRunner.class);
    
    public static void runCommand(String command) {
        if (command.equals("stop")) {
            KotlinHttpServer.stopServer();
        } else if (command.equals("exit")) {
            KotlinHttpServer.stopServer();
            Statistics.getInstance().updateStatistics(true);
            System.exit(0);
        } else if (command.equals("restart")) {
            KotlinHttpServer.stopServer();
            KotlinHttpServer.startServer();
        } else if (command.equals("start")) {
            KotlinHttpServer.startServer();
        } else if (command.startsWith("output")) {
            ServerSettings.OUTPUT_DIRECTORY = ResponseUtils.substringAfter(command, "output ");
            ErrorWriter.writeInfoToConsole("done: " + ServerSettings.OUTPUT_DIRECTORY);
        } else if (command.startsWith("update examples")) {
            ExamplesList.updateList();
            HelpLoader.updateExamplesHelp();
//            ErrorsWriter.writeInfoToConsole("done: examples were updated.");
        } else if (command.startsWith("get port")) {
            ErrorWriter.writeInfoToConsole("server port " + KotlinHttpServer.getPort());
        } else if (command.startsWith("get host")) {
            ErrorWriter.writeInfoToConsole("host: " + KotlinHttpServer.getHost());
        } else if (command.startsWith("timeout")) {
            ServerSettings.TIMEOUT_FOR_EXECUTION = ResponseUtils.substringAfter(command, "timeout ");
            ErrorWriter.writeInfoToConsole("done: " + ServerSettings.TIMEOUT_FOR_EXECUTION);
        } else if (command.equals("help")) {
            StringBuilder builder = new StringBuilder("List of commands:\n");
            //builder.append("java_home pathToJavaHome - without \"\"\n");
            builder.append("exit - to stop server and exit application\n");
            builder.append("start - to start server after stop\n");
            builder.append("stop - to stop server\n");
            builder.append("restart - to restart server\n");
            builder.append("update examples - update examples list after changing\n");
            builder.append("timeout int - set maximum running time for user program\n");
            builder.append("output pathToOutputDir - without \"\", set directory to create a class files until compilation\n");
            //builder.append("kotlinLib pathToKotlinLibDir - without \"\", set path to kotlin library jar files\n");
            //builder.append("host String - without \"\", set hostname for server\n");
            builder.append("get host - get host for server\n");
            //builder.append("port int - without \"\", set port for server\n");
            builder.append("get port - get port for server\n");
            builder.append("help - help\n");
            ErrorWriter.writeInfoToConsole(builder.toString());
        } else {
            ErrorWriter.writeInfoToConsole("Incorrect command: help to look at all options");
        }
    }

    public static void setServerSetting(String setting) {
        if (setting.startsWith("java_home")) {
            ServerSettings.JAVA_HOME = ResponseUtils.substringAfter(setting, "java_home ");
        } else if (setting.startsWith("java_execute")) {
            ServerSettings.JAVA_EXECUTE = ResponseUtils.substringAfter(setting, "java_execute ");
        }  else if (setting.startsWith("host")) {
            ServerSettings.HOST = ResponseUtils.substringAfter(setting, "host ");
        } else if (setting.startsWith("port")) {
            ServerSettings.PORT = ResponseUtils.substringAfter(setting, "port ");
        } else if (setting.startsWith("timeout")) {
            ServerSettings.TIMEOUT_FOR_EXECUTION = ResponseUtils.substringAfter(setting, "timeout ");
        } else if (setting.startsWith("output")) {
            ServerSettings.OUTPUT_DIRECTORY = ResponseUtils.substringAfter(setting, "output ");
        } else if (setting.startsWith("examples")) {
            ServerSettings.EXAMPLES_ROOT = ResponseUtils.substringAfter(setting, "examples ");
        } else if (setting.startsWith("help")) {
            ServerSettings.HELP_ROOT = ResponseUtils.substringAfter(setting, "help ");
        } else if (setting.startsWith("testconnectionoutput")) {
            ServerSettings.TEST_CONNECTION_OUTPUT = ResponseUtils.substringAfter(setting, "testconnectionoutput ");
        } else if (setting.startsWith("max_thread_count")) {
            ServerSettings.MAX_THREAD_COUNT = ResponseUtils.substringAfter(setting, "max_thread_count ");
        } else if (setting.startsWith("rt_jar")) {
            ServerSettings.RT_JAR = ResponseUtils.substringAfter(setting, "rt_jar ");
        }  else if (setting.startsWith("mysql_host")) {
            ServerSettings.MYSQL_HOST = ResponseUtils.substringAfter(setting, "mysql_host ");
        }  else if (setting.startsWith("mysql_port")) {
            ServerSettings.MYSQL_PORT = ResponseUtils.substringAfter(setting, "mysql_port ");
        } else if (setting.startsWith("mysql_database_name")) {
            ServerSettings.MYSQL_DATABASE_NAME = ResponseUtils.substringAfter(setting, "mysql_database_name ");
        } else if (setting.startsWith("mysql_username")) {
            ServerSettings.MYSQL_USERNAME = ResponseUtils.substringAfter(setting, "mysql_username ");
        } else if (setting.startsWith("mysql_password")) {
            ServerSettings.MYSQL_PASSWORD = ResponseUtils.substringAfter(setting, "mysql_password ");
        } else if (setting.startsWith("auth_redirect")) {
            ServerSettings.AUTH_REDIRECT = ResponseUtils.substringAfter(setting, "auth_redirect ");
        }  else if (setting.startsWith("is_test_version")) {
            ServerSettings.IS_TEST_VERSION = ResponseUtils.substringAfter(setting, "is_test_version ");
        } else {
            ErrorWriter.writeErrorToConsole("Incorrect setting in config.properties file: " + setting);
        }

    }
}
