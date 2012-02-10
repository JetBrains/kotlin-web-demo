package org.jetbrains.demo.ukhorskaya.servlet;

import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.Initializer;
import org.jetbrains.demo.ukhorskaya.Statistics;
import org.jetbrains.demo.ukhorskaya.database.MySqlConnector;
import org.jetbrains.demo.ukhorskaya.examplesLoader.ExamplesList;
import org.jetbrains.demo.ukhorskaya.handlers.ServerHandler;
import org.jetbrains.demo.ukhorskaya.help.HelpLoader;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;

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

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();

        new File(ServerSettings.LOGS_ROOT).mkdir();
        new File(ServerSettings.STATISTICS_ROOT).mkdir();

        try {
            if (Initializer.getInstance().initJavaCoreEnvironment()) {
                ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                ExamplesList.getInstance();
                HelpLoader.getInstance();
                Statistics.getInstance();
                MySqlConnector.getInstance();
            } else {
                ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
            }
        } catch (Exception e) {
            ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
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
