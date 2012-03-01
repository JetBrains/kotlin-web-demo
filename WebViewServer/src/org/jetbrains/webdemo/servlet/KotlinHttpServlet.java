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
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.Statistics;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.server.ServerSettings;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 1/16/12
 * Time: 12:17 PM
 */

public class KotlinHttpServlet extends HttpServlet {

    private final ServerHandler myHandler = new ServerHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.setProperty("kotlin.running.in.server.mode", "true");
        System.setProperty("java.awt.headless", "true");

        //loadTomcatParameters();

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();

        new File(ServerSettings.LOGS_ROOT).mkdir();
        new File(ServerSettings.STATISTICS_ROOT).mkdir();

        try {
            if (Initializer.getInstance().initJavaCoreEnvironment()) {
                ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                ExamplesList.getInstance();
                HelpLoader.getInstance();
                Statistics.getInstance();
                //MySqlConnector.getInstance();
            } else {
                ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
            }
        } catch (Exception e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }
    }

    private void loadTomcatParameters() {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            NamingContext envCtx = (NamingContext) initCtx.lookup("java:comp/env");
            int i = 0;
            String str = (String) envCtx.lookup("java_home");
            System.out.println(str);
            /*DataSource ds = (DataSource)
                    envCtx.lookup("jdbc/EmployeeDB");

            Connection conn = ds.getConnection();
            conn.close();*/
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
