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

import com.intellij.lang.java.JavaParserDefinition;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.jet.compiler.CompileEnvironment;
import org.jetbrains.jet.compiler.JetCoreEnvironment;
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
public class ServerInitializer extends Initializer {
    //    private static final Logger LOG = Logger.getLogger(Initializer.class);
    private static ServerInitializer initializer = new ServerInitializer();

    public static ServerInitializer getInstance() {
        return initializer;
    }

    private ServerInitializer() {
    }

    private static JetCoreEnvironment environment;

    public JetCoreEnvironment getEnvironment() {
        if (environment != null) {
            return environment;
        }
        ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("initialize", "JavaCoreEnvironment is null.", "null"));
        return null;
    }

    public boolean setJavaCoreEnvironment() {
        File rtJar = findRtJar();
        if (rtJar == null) {
            ErrorWriterOnServer.writeErrorToConsole("Returned rtJar is null.");
            return false;
        }
        environment.addToClasspath(rtJar);
        if (!initializeKotlinRuntime()) {
            ErrorWriterOnServer.writeInfoToConsole("Cannot found Kotlin Runtime library.");
        }
        environment.registerFileType(JetFileType.INSTANCE, "kt");
        environment.registerFileType(JetFileType.INSTANCE, "kts");
        environment.registerFileType(JetFileType.INSTANCE, "ktm");
        environment.registerFileType(JetFileType.INSTANCE, "jet");
        environment.registerParserDefinition(new JetParserDefinition());
        environment.registerParserDefinition(new JavaParserDefinition());
//        ((MockProject) environment.getProject()).registerService(JavaPsiImplementationHelper.class, new CoreJavaPsiImplementationHelper());

        return true;
    }

    public boolean initializeKotlinRuntime() {
        final File unpackedRuntimePath = getUnpackedRuntimePath();
        if (unpackedRuntimePath != null) {
            ServerSettings.PATH_TO_KOTLIN_LIB = unpackedRuntimePath.getAbsolutePath();
            ErrorWriter.writeInfoToConsole("Kotlin Runtime library found at " + ServerSettings.PATH_TO_KOTLIN_LIB);
            environment.addToClasspath(unpackedRuntimePath);
        } else {
            final File runtimeJarPath = getRuntimeJarPath();
            if (runtimeJarPath != null && runtimeJarPath.exists()) {
                environment.addToClasspath(runtimeJarPath);
                ServerSettings.PATH_TO_KOTLIN_LIB = runtimeJarPath.getAbsolutePath();
                ErrorWriter.writeInfoToConsole("Kotlin Runtime library found at " + ServerSettings.PATH_TO_KOTLIN_LIB);
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

            return setJavaCoreEnvironment();
        }
        ErrorWriterOnServer.writeInfoToConsole("JavaCoreEnvironment is already initialized.");
        return true;
    }

    @Nullable
    private File findRtJar() {
        File rtJar;
        if (!ServerSettings.RT_JAR.equals("")) {
            rtJar = new File(ServerSettings.RT_JAR);
        } else {
            rtJar = CompileEnvironment.findRtJar();
            /*String java_home = ServerSettings.JAVA_HOME;
            ErrorWriterOnServer.LOG_FOR_INFO.info("java_home " + ServerSettings.JAVA_HOME + " " + java_home);
            if (java_home != null) {
                rtJar = findRtJar(java_home);
                if (rtJar == null) {
                    rtJar = findActiveRtJar(true);
                    if (rtJar == null || !rtJar.exists()) {
                        rtJar = findClassesJar(java_home);
                    }
                }
            } else {
                rtJar = findActiveRtJar(true);
            }*/
        }
        if ((rtJar == null || !rtJar.exists())) {
            if (ServerSettings.JAVA_HOME == null) {
                ErrorWriter.writeInfoToConsole("You can set java_home variable at config.properties file.");
            } else {
                ErrorWriter.writeErrorToConsole("No rt.jar found under JAVA_HOME=" + ServerSettings.JAVA_HOME + " or path to rt.jar is incorrect " + ServerSettings.RT_JAR);
            }
            return null;
        }
        return rtJar;
    }

    @Nullable
    private File findRtJar(String javaHome) {
        File rtJar = new File(javaHome, "jre" + File.separatorChar + "lib" + File.separatorChar + "rt.jar");
        ErrorWriterOnServer.LOG_FOR_INFO.info(rtJar.getAbsolutePath() + " exists = " + rtJar.exists());
        if (rtJar.exists()) {
            return rtJar;
        }
        ErrorWriter.writeErrorToConsole("Couldn't found rt.jar in " + rtJar.getAbsolutePath());
        return null;
    }

    @Nullable
    private File findClassesJar(String javaHome) {
        File rtJar = new File(javaHome, "Classes" + File.separatorChar + "classes.jar");
        ErrorWriterOnServer.LOG_FOR_INFO.info(rtJar.getAbsolutePath() + " exists = " + rtJar.exists());
        if (rtJar.exists()) {
            return rtJar;
        }
        ErrorWriter.writeErrorToConsole("Couldn't found classes.jar in " + rtJar.getAbsolutePath());
        return null;
    }

    @Nullable
    private File findActiveRtJar(boolean failOnError) {
        ErrorWriter.writeInfoToConsole("Look for active rt.jar");
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
                ErrorWriter.writeErrorToConsole("Could not find rt.jar in system class loader: " + StringUtil.join(loader.getURLs(), new Function<URL, String>() {
                    @Override
                    public String fun(URL url) {
                        return url.toString();
                    }
                }, ", "));
            }
        } else if (failOnError) {
            ErrorWriter.writeErrorToConsole("System class loader is not an URLClassLoader: " + systemClassLoader);
        }
        ErrorWriter.writeErrorToConsole("Couldn't found classes.jar or rt.jar in systemClassLoader " + systemClassLoader.toString());
        return null;
    }
}


