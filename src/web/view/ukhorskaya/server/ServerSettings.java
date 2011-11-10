package web.view.ukhorskaya.server;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/8/11
 * Time: 1:20 PM
 */

public class ServerSettings {
    public static String JAVA_HOME = System.getenv("JAVA_HOME");
    //TODO now it must endWith "out\\"
    public static String OUTPUT_DIRECTORY = "C:\\Development\\testProject\\out\\";
    public static String PATH_TO_KOTLIN_LIB = "C:\\jet\\jet\\dist\\kotlin-compiler.jar;C:\\jet\\jet\\dist\\kotlin-runtime.jar";
    public static String HOSTNAME = "172.26.240.97";
    public static int TIMEOUT_FOR_EXECUTION = 5000;
    public static int SERVER_PORT = 80;

}
