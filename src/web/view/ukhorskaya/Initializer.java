package web.view.ukhorskaya;

import com.intellij.core.JavaCoreEnvironment;
import com.intellij.openapi.Disposable;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.plugin.JetFileType;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:49 PM
 */
public class Initializer {
    private static Initializer initializer = new Initializer();

    public static Initializer getInstance() {
        return initializer;
    }

    private Initializer() {
    }

    private static JavaCoreEnvironment javaCoreEnvironment;

    public static JavaCoreEnvironment getEnvironment() {
        return javaCoreEnvironment;
    }

    public static void setJavaCoreEnvironment() {
        File rtJar = initJdk();
        if (rtJar == null) return;
        javaCoreEnvironment.addToClasspath(rtJar);
        javaCoreEnvironment.registerFileType(JetFileType.INSTANCE, "kt");
        javaCoreEnvironment.registerFileType(JetFileType.INSTANCE, "jet");
        javaCoreEnvironment.registerParserDefinition(new JetParserDefinition());
    }

    public void initJavaCoreEnvironment() {
        System.setProperty("java.awt.headless", "true");

        Disposable root = new Disposable() {
            @Override
            public void dispose() {
            }
        };
        javaCoreEnvironment = new JavaCoreEnvironment(root);

        setJavaCoreEnvironment();
    }

    public static void setJavaHome(String path) {
        ServerSettings.JAVA_HOME = path;
        setJavaCoreEnvironment();
        ApplicationErrorsWriter.writeInfoToConsole("JAVA_HOME = " + ServerSettings.JAVA_HOME);
    }

    private static File initJdk() {
        File rtJar = null;
        if (ServerSettings.JAVA_HOME == null) {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (systemClassLoader instanceof URLClassLoader) {
                URLClassLoader loader = (URLClassLoader) systemClassLoader;
                for (URL url : loader.getURLs()) {
                    if ("file".equals(url.getProtocol())) {
                        if (url.getFile().endsWith("/lib/rt.jar")) {
                            rtJar = new File(url.getFile());
                            break;
                        }
                        if (url.getFile().endsWith("/Classes/classes.jar")) {
                            rtJar = new File(url.getFile()).getAbsoluteFile();
                            break;
                        }
                    }
                }
            }

            if (rtJar == null) {
                ApplicationErrorsWriter.writeErrorToConsole("JAVA_HOME environment variable needs to be defined");
                return null;
            }
        } else {
            rtJar = findRtJar(ServerSettings.JAVA_HOME);
        }

        if (rtJar == null || !rtJar.exists()) {
            ApplicationErrorsWriter.writeErrorToConsole("No rt.jar found under JAVA_HOME=" + ServerSettings.JAVA_HOME);
            return null;
        }
        return rtJar;
    }

    private static File findRtJar(String javaHome) {
        File rtJar = new File(javaHome, "jre/lib/rt.jar");
        if (rtJar.exists()) {
            return rtJar;
        }
        return null;
    }
}


