package web.view.ukhorskaya;

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
                                Initializer.setJavaHome(tmp.substring(14));
                            } else if (tmp.startsWith("set output")) {
                                JavaRunner.OUTPUT_DIRECTORY = tmp.substring(11);
                            } else if (tmp.equals("-h") || tmp.equals("--help")) {
                                System.out.println("The most commonly used commands are:");
                                System.out.println("set JAVA_HOME pathToJavaHome - without \"\"");
                                System.out.println("set output pathToOutputDir - without \"\", set directory to create a class files until compilation");
                                System.out.println("stop - to stop server and exit application");
                                System.out.println("restart - to restart server");
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error while reading console");
                    }
                }
            }
        });
        t.start();

    }
}
