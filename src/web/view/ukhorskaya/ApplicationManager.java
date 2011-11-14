package web.view.ukhorskaya;

import web.view.ukhorskaya.server.KotlinHttpServer;
import web.view.ukhorskaya.server.ServerSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/14/11
 * Time: 11:54 AM
 */

public class ApplicationManager {
    public static String runCommand(String command) {
        if (command.equals("stop")) {
            KotlinHttpServer.stopServer();
        } else if (command.equals("stopAndExit")) {
            KotlinHttpServer.stopServer();
            System.exit(0);
        } else if (command.equals("restart")) {
            KotlinHttpServer.stopServer();
            KotlinHttpServer.startServer();
        } else if (command.equals("start")) {
            KotlinHttpServer.startServer();
        } else if (command.startsWith("java home")) {
            Initializer.setJavaHome(ResponseUtils.substringAfter(command, "java home "));
            return "done: java home = " + ServerSettings.JAVA_HOME;
        } else if (command.startsWith("set output")) {
            ServerSettings.OUTPUT_DIRECTORY = ResponseUtils.substringAfter(command, "set output ");
            return "done: " + ServerSettings.OUTPUT_DIRECTORY;
        } else if (command.startsWith("set hostname")) {
            ServerSettings.HOSTNAME = ResponseUtils.substringAfter(command, "set hostname ");
            return "done: " + ServerSettings.HOSTNAME;
        } else if (command.startsWith("set port")) {
            ServerSettings.SERVER_PORT = Integer.parseInt(ResponseUtils.substringAfter(command, "set port "));
            return "done: " + ServerSettings.SERVER_PORT;
        } else if (command.startsWith("get port")) {
            return "server port " + ServerSettings.SERVER_PORT;
        } else if (command.startsWith("get hostname")) {
            return "hostname: " + ServerSettings.HOSTNAME;
        } else if (command.startsWith("set timeout")) {
            ServerSettings.TIMEOUT_FOR_EXECUTION = Integer.parseInt(ResponseUtils.substringAfter(command, "set timeout "));
            return "done: " + ServerSettings.TIMEOUT_FOR_EXECUTION;
        } else if (command.startsWith("set kotlinLib")) {
            ServerSettings.PATH_TO_KOTLIN_LIB = ResponseUtils.substringAfter(command, "set kotlinLib ");
            return "done: " + ServerSettings.PATH_TO_KOTLIN_LIB;
        } else if (command.equals("-h") || command.equals("--help")) {
            StringBuilder builder = new StringBuilder("List of commands:\n");
            builder.append("java home pathToJavaHome - without \"\"\n");
            builder.append("set timeout int - set maximum running time for user program\n");
            builder.append("set output pathToOutputDir - without \"\", set directory to create a class files until compilation\n");
            builder.append("set kotlinLib pathToKotlinLibDir - without \"\", set path to kotlin library jar files\n");
            builder.append("set hostname String - without \"\", set hostname for server\n");
            builder.append("get hostname - get hostname for server\n");
            builder.append("set port int - without \"\", set port for server\n");
            builder.append("get port - get port for server\n");
            builder.append("start - to start server after stop\n");
            builder.append("stop - to stop server\n");
            builder.append("stopAndExit - to stop server and exit application\n");
            builder.append("restart - to restart server\n");
            builder.append("-h or --help - help\n");
            return builder.toString();
        } else {
            return "Incorrect command: use -h or --help to look at all options";
        }
        return "Done";
    }
}
