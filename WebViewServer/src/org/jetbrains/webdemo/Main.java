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

package org.jetbrains.webdemo;

import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.servlet.KotlinHttpServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import sun.jdbc.odbc.ee.DataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
                        || (name.equals("java_execute"))
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

    /*private static boolean loadProperties() {

        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
               int i = 0;
            *//*DataSource ds = (DataSource)
                    envCtx.lookup("jdbc/EmployeeDB");

            Connection conn = ds.getConnection();
            conn.close();*//*
        } catch (NamingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }*/
}
