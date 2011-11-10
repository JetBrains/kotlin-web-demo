package web.view.ukhorskaya;

import web.view.ukhorskaya.server.KotlinHttpServer;
import web.view.ukhorskaya.server.ServerSettings;

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

        final KotlinHttpServer kotlinHttpServer = KotlinHttpServer.getInstance();
        kotlinHttpServer.startServer();
        ApplicationErrorsWriter.writeInfoToConsole("Use -h or --help to look at all options");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (System.in.available() > 0) {
                            String tmp = "";
                            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                            tmp = reader.readLine();
                            if (tmp.equals("stop")) {
                                KotlinHttpServer.stopServer();
                            } else if (tmp.equals("stopAndExit")) {
                                KotlinHttpServer.stopServer();
                                System.exit(0);
                            } else if (tmp.equals("restart")) {
                                KotlinHttpServer.stopServer();
                                kotlinHttpServer.startServer();
                            } else if (tmp.equals("start")) {
                                kotlinHttpServer.startServer();
                            } else if (tmp.startsWith("java home")) {
                                Initializer.setJavaHome(ResponseUtils.substringAfter(tmp, "java home "));
                            } else if (tmp.startsWith("set output")) {
                                ServerSettings.OUTPUT_DIRECTORY = ResponseUtils.substringAfter(tmp, "set output ");
                                ApplicationErrorsWriter.writeInfoToConsole("done: " + ServerSettings.OUTPUT_DIRECTORY);
                            } else if (tmp.startsWith("set hostname")) {
                                ServerSettings.HOSTNAME = ResponseUtils.substringAfter(tmp, "set hostname ");
                                ApplicationErrorsWriter.writeInfoToConsole("done: " + ServerSettings.OUTPUT_DIRECTORY);
                            } else if (tmp.startsWith("set timeout")) {
                                ServerSettings.TIMEOUT_FOR_EXECUTION = Integer.parseInt(ResponseUtils.substringAfter(tmp, "set timeout "));
                                ApplicationErrorsWriter.writeInfoToConsole("done: " + ServerSettings.TIMEOUT_FOR_EXECUTION);
                            } else if (tmp.startsWith("set kotlinLib")) {
                                ServerSettings.PATH_TO_KOTLIN_LIB = ResponseUtils.substringAfter(tmp, "set kotlinLib ");
                                ApplicationErrorsWriter.writeInfoToConsole("done: " + ServerSettings.PATH_TO_KOTLIN_LIB);
                            } else if (tmp.equals("-h") || tmp.equals("--help")) {
                                ApplicationErrorsWriter.writeInfoToConsole("List of commands:");
                                ApplicationErrorsWriter.writeInfoToConsole("java home pathToJavaHome - without \"\"");
                                ApplicationErrorsWriter.writeInfoToConsole("set timeout int - set maximum running time for user program");
                                ApplicationErrorsWriter.writeInfoToConsole("set output pathToOutputDir - without \"\", set directory to create a class files until compilation");
                                ApplicationErrorsWriter.writeInfoToConsole("set kotlinLib pathToKotlinLibDir - without \"\", set path to kotlin library jar files");
                                ApplicationErrorsWriter.writeInfoToConsole("set hostname String - without \"\", set hostname for server");
                                ApplicationErrorsWriter.writeInfoToConsole("start - to start server");
                                ApplicationErrorsWriter.writeInfoToConsole("stop - to stop server");
                                ApplicationErrorsWriter.writeInfoToConsole("stopAndExit - to stop server and exit application");
                                ApplicationErrorsWriter.writeInfoToConsole("restart - to restart server");
                                ApplicationErrorsWriter.writeInfoToConsole("-h or --help - help");
                            } else {
                                ApplicationErrorsWriter.writeInfoToConsole("Incorrect command: use -h or --help to look at all options");
                            }
                        }
                    } catch (Exception e) {
                        ApplicationErrorsWriter.writeExceptionToConsole(e);
                    }
                }
            }
        });
        t.start();
    }
}
