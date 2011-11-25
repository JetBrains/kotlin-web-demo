package web.view.ukhorskaya.server;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/8/11
 * Time: 1:20 PM
 */

public class ServerSettings {
    public static String JAVA_HOME = System.getenv("JAVA_HOME");
    public static String OUTPUT_DIRECTORY = "out";
    public static String PATH_TO_KOTLIN_LIB = "";
    public static String HOST = "localhost";
    public static String TIMEOUT_FOR_EXECUTION = "5000";
    public static String PORT = "80";
    public static String EXAMPLES_ROOT = "examples";
    public static String HELP_FOR_EXAMPLES = "helpExamples.xml";
    public static String HELP_FOR_WORDS = "helpWords.xml";
    public static String HELP_ROOT = "help";

    public static String KOTLIN_ERROR_MESSAGE = "Exception in Kotlin compiler: a bug was reported to developers.";
    public static String KOTLIN_VERSION = "0.1.60";
}
