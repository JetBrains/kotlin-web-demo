package web.view.ukhorskaya;

import com.intellij.core.JavaCoreEnvironment;
import com.intellij.openapi.Disposable;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.plugin.JetFileType;

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

    private static String javaHome;

    public static Initializer getInstance() {
        return initializer;
    }

    private static JavaCoreEnvironment javaCoreEnvironment;

    public static JavaCoreEnvironment getEnvironment() {
        return javaCoreEnvironment;
    }

    public void initJavaCoreEnvironment() {
        System.setProperty("java.awt.headless", "true");

        Disposable root = new Disposable() {
            @Override
            public void dispose() {
            }
        };
        javaCoreEnvironment = new JavaCoreEnvironment(root);

        File rtJar = initJdk();
        if (rtJar == null) return;

        javaCoreEnvironment.addToClasspath(rtJar);

        javaCoreEnvironment.registerFileType(JetFileType.INSTANCE, "kt");
        javaCoreEnvironment.registerFileType(JetFileType.INSTANCE, "jet");
        javaCoreEnvironment.registerParserDefinition(new JetParserDefinition());
    }

    public static void reInitJavaCoreEnvironment() {
        File rtJar = initJdk();
        if (rtJar == null) return;
        javaCoreEnvironment.addToClasspath(rtJar);
    }

    private Initializer() {
    }

    public static void setJavaHome(String path) {
        javaHome = path;
        reInitJavaCoreEnvironment();
        System.out.println("JAVA_HOME = " + javaHome);
    }

    private static File initJdk() {
        //TODO set javaHome variable
        if (javaHome == null) {
            javaHome = System.getenv("JAVA_HOME");
        }
        File rtJar = null;
        if (javaHome == null) {
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
                System.out.println("JAVA_HOME environment variable needs to be defined");
                return null;
            }
        } else {
            rtJar = findRtJar(javaHome);
        }

        if (rtJar == null || !rtJar.exists()) {
            System.out.println("No rt.jar found under JAVA_HOME=" + javaHome);
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


