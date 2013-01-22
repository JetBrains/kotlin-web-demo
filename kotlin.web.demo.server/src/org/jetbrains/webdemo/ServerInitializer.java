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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.cli.jvm.JVMConfigurationKeys;
import org.jetbrains.jet.cli.jvm.K2JVMCompiler;
import org.jetbrains.jet.cli.jvm.K2JVMCompilerArguments;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.codegen.BuiltinToJavaTypesMapping;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.jet.internal.com.google.common.base.Splitter;
import org.jetbrains.jet.internal.com.google.common.collect.Lists;
import org.jetbrains.jet.internal.com.intellij.openapi.Disposable;
import org.jetbrains.jet.internal.com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.jet.internal.com.intellij.openapi.fileTypes.FileTypeRegistry;
import org.jetbrains.jet.internal.com.intellij.openapi.util.Getter;
import org.jetbrains.jet.internal.com.intellij.openapi.vfs.encoding.EncodingRegistry;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.utils.PathUtil;
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class ServerInitializer extends Initializer {
    private static ServerInitializer initializer = new ServerInitializer();

    private static Getter<FileTypeRegistry> registry;
    private static Disposable root;

    public static ServerInitializer getInstance() {
        return initializer;
    }

    private ServerInitializer() {
    }

    private static JetCoreEnvironment environment;

    @Nullable
    public JetCoreEnvironment getEnvironment() {
        if (environment != null) {
            return environment;
        }

        ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("initialize", "JavaCoreEnvironment is null.", "null"));
        return null;
    }

    @Override
    public Getter<FileTypeRegistry> getRegistry() {
        return registry;
    }

    @Override
    public Disposable getRoot() {
        return root;
    }

    public boolean initJavaCoreEnvironment() {
        if (environment == null) {
            root = new Disposable() {
                @Override
                public void dispose() {
                }
            };

            try {
                K2JVMCompilerArguments arguments = new K2JVMCompilerArguments();
                CompilerConfiguration configuration = new CompilerConfiguration();
                configuration.addAll(JVMConfigurationKeys.CLASSPATH_KEY, getClasspath(arguments));
                configuration.addAll(JVMConfigurationKeys.ANNOTATIONS_PATH_KEY, getAnnotationsPath());

                configuration.put(JVMConfigurationKeys.SCRIPT_PARAMETERS, Collections.<AnalyzerScriptParameter>emptyList());
                configuration.put(JVMConfigurationKeys.STUBS, false);
                configuration.put(JVMConfigurationKeys.BUILTIN_TO_JAVA_TYPES_MAPPING_KEY, BuiltinToJavaTypesMapping.ENABLED);

                environment = new JetCoreEnvironment(root, configuration);
                registry = FileTypeRegistry.ourInstanceGetter;
            } catch (Throwable e) {
                ErrorWriter.writeExceptionToConsole("Impossible to init jetCoreEnvironment", e);
                return false;
            }

            return true;
        }
        return true;
    }

    @Nullable
    private File initializeKotlinRuntime() {
        final File unpackedRuntimePath = getUnpackedRuntimePath();
        if (unpackedRuntimePath != null) {
            ApplicationSettings.KOTLIN_LIB = unpackedRuntimePath.getAbsolutePath();
            ErrorWriter.writeInfoToConsole("Kotlin Runtime library founded at " + ApplicationSettings.KOTLIN_LIB);
            return unpackedRuntimePath;
        }
        else {
            final File runtimeJarPath = getRuntimeJarPath();
            if (runtimeJarPath != null && runtimeJarPath.exists()) {
                ApplicationSettings.KOTLIN_LIB = runtimeJarPath.getAbsolutePath();
                ErrorWriter.writeInfoToConsole("Kotlin Runtime library founded at " + ApplicationSettings.KOTLIN_LIB);
                return runtimeJarPath;
            }
        }
        return null;
    }

    @Nullable
    private File getUnpackedRuntimePath() {
        URL url = K2JVMCompiler.class.getClassLoader().getResource("jet/JetObject.class");
        if (url != null && url.getProtocol().equals("file")) {
            return new File(url.getPath()).getParentFile().getParentFile();
        }
        return null;
    }

    @Nullable
    private File getRuntimeJarPath() {
        URL url = K2JVMCompiler.class.getClassLoader().getResource("kotlin/KotlinPackage.class");
        if (url != null && url.getProtocol().equals("jar")) {
            String path = url.getPath();
            return new File(path.substring(path.indexOf(":") + 1, path.indexOf("!/")));
        }
        return null;
    }

    @NotNull
    private List<File> getClasspath(@NotNull K2JVMCompilerArguments arguments) {
        List<File> classpath = Lists.newArrayList();
        classpath.add(findRtJar());

        classpath.add(initializeKotlinRuntime());
        if (arguments.classpath != null) {
            for (String element : Splitter.on(File.pathSeparatorChar).split(arguments.classpath)) {
                classpath.add(new File(element));
            }
        }
        return classpath;
    }

    @NotNull
    private List<File> getAnnotationsPath() {
        List<File> annotationsPath = Lists.newArrayList();
        annotationsPath.add(PathUtil.getKotlinPathsForCompiler().getJdkAnnotationsPath());
        return annotationsPath;
    }

    public static void reinitializeJavaEnvironment() {
        ApplicationManager.setApplication(environment.getApplication(), registry, EncodingRegistry.ourInstanceGetter, root);
    }

    @Nullable
    private static File findRtJar() {
        File rtJar;
        if (!ApplicationSettings.RT_JAR.equals("")) {
            rtJar = new File(ApplicationSettings.RT_JAR);
        }
        else {
            rtJar = PathUtil.findRtJar();
        }
        if ((rtJar == null || !rtJar.exists())) {
            if (ApplicationSettings.JAVA_HOME == null) {
                ErrorWriter.writeInfoToConsole("You can set java_home variable at config.properties file.");
            }
            else {
                ErrorWriter.writeErrorToConsole("No rt.jar found under JAVA_HOME=" + ApplicationSettings.JAVA_HOME + " or path to rt.jar is incorrect " + ApplicationSettings.RT_JAR);
            }
            return null;
        }
        ApplicationSettings.JAVA_HOME = rtJar.getParentFile().getParentFile().getParentFile().getAbsolutePath();
        return rtJar;
    }

}


