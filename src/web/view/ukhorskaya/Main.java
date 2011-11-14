package web.view.ukhorskaya;

import web.view.ukhorskaya.server.KotlinHttpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:35 PM
 */

public class Main {


    public static void main(String[] args) {
        try {
            Initializer.getInstance().initJavaCoreEnvironment();
        } catch (Exception e) {
            ApplicationErrorsWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
            System.exit(1);
        }

        KotlinHttpServer.startServer();

        ApplicationErrorsWriter.writeInfoToConsole("Use -h or --help to look at all options");

        Thread consoleListener = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (System.in.available() > 0) {
                            String tmp = "";
                            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                            tmp = reader.readLine();
                            ApplicationErrorsWriter.writeInfoToConsole(ApplicationManager.runCommand(tmp));
                        }
                    } catch (Exception e) {
                        ApplicationErrorsWriter.writeExceptionToConsole(e);
                    }
                }
            }
        });
        consoleListener.start();

    }
}
