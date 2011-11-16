package web.view.ukhorskaya;

import web.view.ukhorskaya.server.KotlinHttpServer;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:35 PM
 */

public class Main {


    public static void main(String[] args) {
        if (args.length == 2) {
            ServerSettings.HOST = args[0];
            ApplicationErrorsWriter.writeInfoToConsole("Host is set: " + args[0]);
            ServerSettings.PORT = args[1];
            ApplicationErrorsWriter.writeInfoToConsole("Port is set: " + args[1]);
        }
        new File("logs").mkdir();
        //new File("out").mkdir();

        if (loadProperties()) {

            try {
                if (Initializer.getInstance().initJavaCoreEnvironment()) {
                    KotlinHttpServer.startServer();
                    ApplicationErrorsWriter.writeInfoToConsole("Use \"help\" to look at all options");
                    startConsoleThread();
                } else {
                    ApplicationErrorsWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
                }
            } catch (Exception e) {
                ApplicationErrorsWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
                System.exit(1);
            }

        } else {
            ApplicationErrorsWriter.writeErrorToConsole("Can not find config.properties.");
        }
    }

    private static void startConsoleThread() {
        System.out.print("> ");
        Thread consoleListener = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (System.in.available() > 0) {
                            String tmp = "";
                            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                            tmp = reader.readLine();
                            ApplicationManager.runCommand(tmp);
                            System.out.print("> ");
                        }
                    } catch (Exception e) {
                        ApplicationErrorsWriter.writeExceptionToConsole(e);
                    }
                }
            }
        });
        consoleListener.start();
    }

    private static boolean loadProperties() {
        File file = new File("config.properties");
        if (!file.exists()) {
            ApplicationErrorsWriter.writeErrorToConsole(file.getAbsolutePath());
            return false;
        }

        try {
            Properties properties = new Properties();
            properties.load(new FileReader(file));
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                String value;
                if ((name.equals("java_home")) || (name.equals("output")) || (name.equals("examples"))) {
                    value = properties.get(name).toString();
                    value = value.substring(1, value.length() - 1);
                } else {
                    value = properties.get(name).toString();
                }

                ApplicationManager.setServerSetting(name + " " + value);
                ApplicationErrorsWriter.writeInfoToConsole("Loaded from config file: " + name + " " + value);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
