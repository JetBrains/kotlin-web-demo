package web.view.ukhorskaya;

import web.view.ukhorskaya.server.KotlinHttpServer;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.BufferedReader;
import java.io.IOException;
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
            System.err.println("FATAL ERROR: Initialisation of java core environment failed, server didn't start");
            e.printStackTrace();
            System.exit(1);
        }

        final KotlinHttpServer kotlinHttpServer = KotlinHttpServer.getInstance();
        kotlinHttpServer.startServer();

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
                                System.exit(0);
                            } else if (tmp.equals("restart")) {
                                KotlinHttpServer.stopServer();
                                kotlinHttpServer.startServer();
                            } else if (tmp.startsWith("set JAVA_HOME")) {
                                Initializer.setJavaHome(ResponseUtils.substringAfter(tmp, "set JAVA_HOME "));
                            } else if (tmp.startsWith("set output")) {
                                ServerSettings.OUTPUT_DIRECTORY = ResponseUtils.substringAfter(tmp, "set output ");
                                System.out.println("done: " + ServerSettings.OUTPUT_DIRECTORY);
                            } else if (tmp.startsWith("set timeout")) {
                                ServerSettings.TIMEOUT_FOR_EXECUTION = Integer.parseInt(ResponseUtils.substringAfter(tmp, "set timeout "));
                                System.out.println("done: " + ServerSettings.TIMEOUT_FOR_EXECUTION);
                            } else if (tmp.startsWith("set kotlinLib")) {
                                ServerSettings.PATH_TO_KOTLIN_LIB = ResponseUtils.substringAfter(tmp, "set kotlinLib ");
                                System.out.println("done: " + ServerSettings.PATH_TO_KOTLIN_LIB);
                            } else if (tmp.equals("-h") || tmp.equals("--help")) {
                                System.out.println("List of commands:");
                                System.out.println("set JAVA_HOME pathToJavaHome - without \"\"");
                                System.out.println("set        timeout int - set timeout for execution");
                                System.out.println("set output pathToOutputDir - without \"\", set directory to create a class files until compilation");
                                System.out.println("set kotlinLib pathToKotlinLibDir - without \"\", set path to kotlin library jar files");
                                System.out.println("stop - to stop server and exit application");
                                System.out.println("restart - to restart server");
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error while reading console");
                    }
                }
            }
        });
        t.start();
    }
}
