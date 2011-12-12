package web.view.ukhorskaya;

import web.view.ukhorskaya.examplesLoader.ExamplesList;
import web.view.ukhorskaya.help.HelpLoader;
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
        System.setProperty("kotlin.running.in.server.mode", "true");

        ErrorWriter.ERROR_WRITER = ErrorWriterOnServer.getInstance();

        if (args.length == 2) {
            ServerSettings.HOST = args[0];
            ErrorWriter.writeInfoToConsole("Host is set: " + args[0]);
            ServerSettings.PORT = args[1];
            ErrorWriter.writeInfoToConsole("Port is set: " + args[1]);
        }
        new File("logs").mkdir();

        if (loadProperties()) {
            try {
                if (Initializer.getInstance().initJavaCoreEnvironment()) {
                    KotlinHttpServer.startServer();
                    ErrorWriter.writeInfoToConsole("Use \"help\" to look at all options");
                    ExamplesList.getInstance();
                    HelpLoader.getInstance();
                    Statistics.getInstance();
                    startConsoleThread();
                } else {
                    ErrorWriter.writeErrorToConsole("Initialisation of java core environment failed, server didn't start.");
                }
            } catch (Exception e) {
                ErrorWriter.writeExceptionToConsole("FATAL ERROR: Initialisation of java core environment failed, server didn't start", e);
                System.exit(1);
            }

        } else {
            ErrorWriter.writeErrorToConsole("Can not find config.properties.");
        }
    }

    private static void startConsoleThread() {
        System.out.print("> ");
        Thread consoleListener = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String tmp = "";
                while (true) {
                    try {
                        Thread.sleep(100);
                        if (System.in.available() > 0) {
                            tmp = reader.readLine();
                            CommandRunner.runCommand(tmp);
                            System.out.print("> ");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /*String tmp = "";
                try {
                    while ((tmp = reader.readLine()) != null) {
                        CommandRunner.runCommand(tmp);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    ErrorsWriter.writeExceptionToConsole(e);
                }*/
            }
        });
        consoleListener.start();
    }

    private static boolean loadProperties() {
        File file = new File("config.properties");
        if (!file.exists()) {
            ErrorWriter.writeErrorToConsole(file.getAbsolutePath());
            return false;
        }

        try {
            Properties properties = new Properties();
            properties.load(new FileReader(file));
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                String value;
                if ((name.equals("java_home")) || (name.equals("output"))
                        || (name.equals("examples")) || (name.equals("help"))
                        || (name.equals("testconnectionoutput"))
                        || (name.equals("rt_jar"))) {
                    value = properties.get(name).toString();
                    value = value.substring(1, value.length() - 1);
                } else {
                    value = properties.get(name).toString();
                }

                CommandRunner.setServerSetting(name + " " + value);
                ErrorWriter.writeInfoToConsole("Loaded from config file: " + name + " " + value);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
