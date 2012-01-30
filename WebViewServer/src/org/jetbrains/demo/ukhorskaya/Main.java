package org.jetbrains.demo.ukhorskaya;

import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.servlet.KotlinHttpServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        try {
            if (loadProperties()) {
                Server server = new Server(Integer.parseInt(ServerSettings.PORT));
                Context root = new Context(server, "/", Context.SESSIONS);
                root.addServlet(new ServletHolder(new KotlinHttpServlet()), "/*");
                server.start();
            } else {
                ErrorWriter.writeErrorToConsole("Can not find config.properties.");
                ErrorWriter.writeErrorToConsole("Server didn't start");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
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
