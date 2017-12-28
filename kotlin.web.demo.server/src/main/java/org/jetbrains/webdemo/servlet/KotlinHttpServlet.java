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

package org.jetbrains.webdemo.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.loader.ExamplesLoader;
import org.jetbrains.webdemo.loader.KotlinKoansLoader;

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
import java.util.ArrayList;
import java.util.List;

public class KotlinHttpServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(KotlinHttpServlet.class);
    private final ServerHandler myHandler = new ServerHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        CommonSettings.WEBAPP_ROOT_DIRECTORY = getServletContext().getRealPath("/");
        ApplicationSettings.EXAMPLES_DIRECTORY = CommonSettings.WEBAPP_ROOT_DIRECTORY + "examples";
        CommonSettings.HELP_DIRECTORY = CommonSettings.WEBAPP_ROOT_DIRECTORY;

        if (!loadTomcatParameters()) {
            log.fatal("FATAL ERROR: Cannot load parameters from tomcat config, server didn't start");
        }

        ErrorWriter.ERROR_WRITER = ErrorWriter.getInstance();
//        Initializer.INITIALIZER = ServerInitializer.getInstance();

        try {
            new File(CommonSettings.LOGS_DIRECTORY).mkdirs();
            LogWriter.init();
            ExamplesLoader.loadAllExamples();
            KotlinKoansLoader kotlinKoansLoader = new KotlinKoansLoader();
            kotlinKoansLoader.loadKotlinKoansStructure();
            HelpLoader.getInstance();
            MySqlConnector.getInstance();
            MySqlConnector.getInstance().createTaskList(getTaskList(ExamplesFolder.ROOT_FOLDER));
        } catch (Throwable e) {
            log.fatal("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
        }
    }

    private List<String> getTaskList(ExamplesFolder folder) {
        List<String> tasksIdentifiers = new ArrayList<>();

        for (ExamplesFolder childFolder : folder.getChildFolders()) {
            tasksIdentifiers.addAll(getTaskList(childFolder));
        }
        if (!folder.isTaskFolder()) return tasksIdentifiers;

        for (Project example : folder.getExamples()) {
            tasksIdentifiers.add(example.id);
        }
        return tasksIdentifiers;
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

            CommandRunner.setServerSettingFromTomcatConfig("backend_url", (String) envCtx.lookup("backend_url"));
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
