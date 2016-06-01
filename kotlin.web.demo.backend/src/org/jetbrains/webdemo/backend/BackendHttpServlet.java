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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.webdemo.CommonSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.LogWriter;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;

import javax.naming.Context;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class BackendHttpServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(BackendHttpServlet.class);
    private final ServerHandler myHandler = new ServerHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.setProperty("java.awt.headless", "true");

        CommonSettings.WEBAPP_ROOT_DIRECTORY = getServletContext().getRealPath("/");
        BackendSettings.CLASS_PATH = CommonSettings.WEBAPP_ROOT_DIRECTORY + "WEB-INF" + File.separator + "classes";
        BackendSettings.LIBS_DIR = CommonSettings.WEBAPP_ROOT_DIRECTORY + "WEB-INF" + File.separator + "lib";
        BackendSettings.KOTLIN_LIBS_DIR = BackendSettings.LIBS_DIR;

        if (!loadTomcatParameters()) {
            log.fatal("FATAL ERROR: Cannot load parameters from tomcat config, server didn't start");
        }

        Path wrappersDir = Paths.get(CommonSettings.WEBAPP_ROOT_DIRECTORY, "WEB-INF", "kotlin-wrappers");
        Path junitLib = Paths.get(BackendSettings.LIBS_DIR, "junit-4.12.jar");
        KotlinWrappersManager.init(wrappersDir, Collections.singletonList(junitLib), Paths.get("classes"));
        LogWriter.init();
        ErrorWriter.getInstance();

        try {
            Initializer.initializeExecutorsPolicyFile();
        } catch (Throwable e) {
            log.fatal("FATAL ERROR: Initialisation of executors policy file failed, server didn't start", e);
            System.exit(1);
        }
    }

    private boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().contains("win"));
    }

    private boolean loadTomcatParameters() {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            try {
                CommandRunner.setServerSettingFromTomcatConfig("java_home", (String) envCtx.lookup("java_home"));
            } catch (NamingException e) {
                CommandRunner.setServerSettingFromTomcatConfig("java_home", System.getenv("JAVA_HOME"));
            }
            try {
                CommandRunner.setServerSettingFromTomcatConfig("java_execute", (String) envCtx.lookup("java_execute"));
            } catch (NamingException e) {
                String executable = isWindows() ? "java.exe" : "java";
                CommandRunner.setServerSettingFromTomcatConfig("java_execute", BackendSettings.JAVA_HOME + File.separator + "bin" + File.separator + executable);
            }
            try {
                CommandRunner.setServerSettingFromTomcatConfig("app_output_dir", (String) envCtx.lookup("app_output_dir"));
            } catch (NamingException e) {
                File rootFolder = new File(CommonSettings.WEBAPP_ROOT_DIRECTORY);
                String appHome = rootFolder.getParentFile().getParentFile().getParent();
                CommandRunner.setServerSettingFromTomcatConfig("app_output_dir", appHome);
            }

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
