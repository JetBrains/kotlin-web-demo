package web.view.ukhorskaya;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetCoreEnvironment;
import org.jetbrains.jet.compiler.CompileEnvironment;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.plugin.JetFileType;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

//import web.view.ukhorskaya.server.ServerSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:49 PM
 */
public class InitializerApplet {
    //    private static final Logger LOG = Logger.getLogger(Initializer.class);
    private static InitializerApplet initializer = new InitializerApplet();

    public static InitializerApplet getInstance() {
        return initializer;
    }

    private InitializerApplet() {
    }

    private static JetCoreEnvironment environment;

    public static JetCoreEnvironment getEnvironment() {
        if (environment != null) {
            return environment;
        }
        return null;
    }

    public boolean initializeKotlinRuntime() {
        final File unpackedRuntimePath = getUnpackedRuntimePath();
        if (unpackedRuntimePath != null) {
//            ServerSettings.PATH_TO_KOTLIN_LIB = unpackedRuntimePath.getAbsolutePath();
//            ErrorsWriter.sendErrorToServer("Kotlin Runtime library found at " + ServerSettings.PATH_TO_KOTLIN_LIB);
            environment.addToClasspath(unpackedRuntimePath);
        } else {
            final File runtimeJarPath = getRuntimeJarPath();
            if (runtimeJarPath != null && runtimeJarPath.exists()) {
                environment.addToClasspath(runtimeJarPath);
//                ServerSettings.PATH_TO_KOTLIN_LIB = runtimeJarPath.getAbsolutePath();
//                ErrorsWriter.sendErrorToServer("Kotlin Runtime library found at " + ServerSettings.PATH_TO_KOTLIN_LIB);
            } else {
                return false;
            }
        }
        return true;
    }

    public static File getUnpackedRuntimePath() {
        URL url = CompileEnvironment.class.getClassLoader().getResource("jet/JetObject.class");
        if (url != null && url.getProtocol().equals("file")) {
            return new File(url.getPath()).getParentFile().getParentFile();
        }
        return null;
    }

    public static File getRuntimeJarPath() {
        URL url = CompileEnvironment.class.getClassLoader().getResource("jet/JetObject.class");
        if (url != null && url.getProtocol().equals("jar")) {
            String path = url.getPath();
            return new File(path.substring(path.indexOf(":") + 1, path.indexOf("!/")));
        }
        return null;
    }

    public boolean initJavaCoreEnvironment() {
        if (environment == null) {

            Disposable root = new Disposable() {
                @Override
                public void dispose() {
                }
            };
            environment = new JetCoreEnvironment(root);

            /*if (!initializeKotlinRuntime()) {
                ErrorsWriter.sendErrorToServer("Cannot found Kotlin Runtime library.");
            }*/
            environment.registerFileType(JetFileType.INSTANCE, "kt");
            environment.registerFileType(JetFileType.INSTANCE, "kts");
            environment.registerFileType(JetFileType.INSTANCE, "ktm");
            environment.registerFileType(JetFileType.INSTANCE, "jet");
            environment.registerParserDefinition(new JetParserDefinition());


            return true;
            //return setJavaCoreEnvironment();
        }
        return false;
    }

    @Nullable
    private File findRtJar() {
        String java_home = null;
//        String java_home = ServerSettings.JAVA_HOME;
//        ErrorsWriter.sendErrorToServer("java_home " + ServerSettings.JAVA_HOME + " " + java_home);
        File rtJar;
        if (java_home != null) {
            rtJar = findRtJar(java_home);
            if (rtJar == null) {
                //rtJar = findActiveRtJar(true);
                if (rtJar == null || !rtJar.exists()) {
                    rtJar = findClassesJar(java_home);
                }
            }
        } else {
            rtJar = null;
            //rtJar = findActiveRtJar(true);
        }

        if ((rtJar == null || !rtJar.exists())) {
            if (java_home == null) {
                ErrorsWriter.sendErrorToServer("You can set java_home variable at config.properties file.");
            } else {
                ErrorsWriter.sendErrorToServer("No rt.jar found under JAVA_HOME=" + java_home);
            }
            return null;
        }
        return rtJar;
    }

    @Nullable
    private File findRtJar(String javaHome) {
        File rtJar = new File(javaHome, "jre" + File.separatorChar + "lib" + File.separatorChar + "rt.jar");
        ErrorsWriter.sendErrorToServer(rtJar.getAbsolutePath() + " exists = " + rtJar.exists());
        if (rtJar.exists()) {
            return rtJar;
        }
        ErrorsWriter.sendErrorToServer("Couldn't found rt.jar in " + rtJar.getAbsolutePath());
        return null;
    }

    @Nullable
    private File findClassesJar(String javaHome) {
        File rtJar = new File(javaHome, "Classes" + File.separatorChar + "classes.jar");
        ErrorsWriter.sendErrorToServer(rtJar.getAbsolutePath() + " exists = " + rtJar.exists());
        if (rtJar.exists()) {
            return rtJar;
        }
        ErrorsWriter.sendErrorToServer("Couldn't found classes.jar in " + rtJar.getAbsolutePath());
        return null;
    }

    @Nullable
    private File findActiveRtJar(boolean failOnError) {
        ErrorsWriter.sendErrorToServer("Look for active rt.jar");
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if (systemClassLoader instanceof URLClassLoader) {
            URLClassLoader loader = (URLClassLoader) systemClassLoader;
            for (URL url : loader.getURLs()) {
                if ("file".equals(url.getProtocol())) {
                    if (url.getFile().endsWith("/lib/rt.jar")) {
                        return new File(url.getFile().replaceAll("%20", " "));
                    }
                    if (url.getFile().endsWith("/Classes/classes.jar")) {
                        return new File(url.getFile()).getAbsoluteFile();
                    }
                }
            }
            if (failOnError) {
                ErrorsWriter.sendErrorToServer("Could not find rt.jar in system class loader: " + StringUtil.join(loader.getURLs(), new Function<URL, String>() {
                    @Override
                    public String fun(URL url) {
                        return url.toString();
                    }
                }, ", "));
            }
        } else if (failOnError) {
            ErrorsWriter.sendErrorToServer("System class loader is not an URLClassLoader: " + systemClassLoader);
        }
        ErrorsWriter.sendErrorToServer("Couldn't found classes.jar or rt.jar in systemClassLoader " + systemClassLoader.toString());
        return null;
    }
}


