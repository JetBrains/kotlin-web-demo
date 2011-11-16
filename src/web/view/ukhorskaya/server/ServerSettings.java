package web.view.ukhorskaya.server;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/8/11
 * Time: 1:20 PM
 */

public class ServerSettings {
    public static String JAVA_HOME = System.getenv("JAVA_HOME");
    public static String OUTPUT_DIRECTORY = "out";
    //public static String PATH_TO_KOTLIN_LIB = System.getProperty("java.class.path");
    public static String PATH_TO_KOTLIN_LIB = "C:" + File.separator + "jet" + File.separator + "jet" + File.separator + "dist" + File.separator + "kotlin-runtime.jar;";
    public static String HOST = "localhost";
    public static String TIMEOUT_FOR_EXECUTION = "5000";
    public static String PORT = "80";
    public static String EXAMPLES_ROOT = "c:\\Development\\git-contrib\\jet-contrib\\WebView\\examples\\";


}
