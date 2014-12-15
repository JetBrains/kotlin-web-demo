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

package org.jetbrains.webdemo.servlet;

import org.apache.naming.NamingContext;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.server.ApplicationSettings;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class KotlinHttpServlet extends HttpServlet {

    private final ServerHandler myHandler = new ServerHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        ApplicationSettings.WEBAPP_ROOT_DIRECTORY = getServletContext().getRealPath("/");
        ApplicationSettings.LIBS_DIR = ApplicationSettings.WEBAPP_ROOT_DIRECTORY + "WEB-INF" + File.separator + "lib" + File.separator;
        ApplicationSettings.EXAMPLES_DIRECTORY = ApplicationSettings.WEBAPP_ROOT_DIRECTORY + "examples";
        ApplicationSettings.HELP_DIRECTORY = ApplicationSettings.WEBAPP_ROOT_DIRECTORY;

        if (!loadTomcatParameters()) {
            ErrorWriter.writeErrorToConsole("FATAL ERROR: Cannot load parameters from tomcat config, server didn't start");
            System.exit(1);
        }

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();
        Initializer.INITIALIZER = ServerInitializer.getInstance();

        try {
            if (ServerInitializer.getInstance().initJavaCoreEnvironment()) {
                ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                new File(ApplicationSettings.LOGS_DIRECTORY).mkdirs();
                new File(ApplicationSettings.STATISTICS_DIRECTORY).mkdirs();
                ExamplesList.getInstance();
                HelpLoader.getInstance();
                Statistics.getInstance();
                MySqlConnector.getInstance();
            } else {
                ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
            }
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }

        try {
            ServerInitializer.getInstance().initializeExecutorsPolicyFile();
        } catch (Throwable e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of executors policy file failed, server didn't start", e);
            System.exit(1);
        }
    }

    private boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }

    private boolean loadTomcatParameters() {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            NamingContext envCtx = (NamingContext) initCtx.lookup("java:comp/env");
            try {
                CommandRunner.setServerSettingFromTomcatConfig("java_home", (String) envCtx.lookup("java_home"));
            } catch (NamingException e) {
                CommandRunner.setServerSettingFromTomcatConfig("java_home", System.getenv("JAVA_HOME"));
            }
            try {
                CommandRunner.setServerSettingFromTomcatConfig("java_execute", (String) envCtx.lookup("java_execute"));
            } catch (NamingException e) {
                String executable = isWindows() ? "java.exe" : "java";
                CommandRunner.setServerSettingFromTomcatConfig("java_execute", ApplicationSettings.JAVA_HOME + File.separator + "bin" + File.separator + executable);
            }
            try {
                CommandRunner.setServerSettingFromTomcatConfig("app_home", (String) envCtx.lookup("app_home"));
            } catch (NamingException e) {
                File rootFolder = new File(ApplicationSettings.WEBAPP_ROOT_DIRECTORY);
                String appHome = rootFolder.getParentFile().getParentFile().getParent();
                CommandRunner.setServerSettingFromTomcatConfig("app_home", appHome);
            }
            CommandRunner.setServerSettingFromTomcatConfig("auth_redirect", (String) envCtx.lookup("auth_redirect"));
            CommandRunner.setServerSettingFromTomcatConfig("google_key", (String) envCtx.lookup("google_key"));
            CommandRunner.setServerSettingFromTomcatConfig("google_secret", (String) envCtx.lookup("google_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("twitter_key", (String) envCtx.lookup("twitter_key"));
            CommandRunner.setServerSettingFromTomcatConfig("twitter_secret", (String) envCtx.lookup("twitter_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("facebook_key", (String) envCtx.lookup("facebook_key"));
            CommandRunner.setServerSettingFromTomcatConfig("facebook_secret", (String) envCtx.lookup("facebook_secret"));

            try {
                CommandRunner.setServerSettingFromTomcatConfig("is_test_version", (String) envCtx.lookup("is_test_version"));
            } catch (NameNotFoundException e) {
                //Absent is_test_version variable in context.xml
                CommandRunner.setServerSettingFromTomcatConfig("is_test_version", "false");
            }
            try {
                CommandRunner.setServerSettingFromTomcatConfig("timeout", (String) envCtx.lookup("timeout"));
            } catch (NameNotFoundException e) {
                //Absent timeout variable in context.xml
            }

            return true;
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        myHandler.handle(request, response);
    }

    @Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }

}
