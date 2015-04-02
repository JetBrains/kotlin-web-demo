/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package org.jetbrains.webdemo.backend.enviroment;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Getter;
import com.intellij.psi.compiled.ClassFileDecompilers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.jvm.JVMConfigurationKeys;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.idea.decompiler.JetClassFileDecompiler;
import org.jetbrains.kotlin.js.analyze.SuppressUnusedParameterForJsNative;
import org.jetbrains.kotlin.js.resolve.diagnostics.DefaultErrorMessagesJs;
import org.jetbrains.kotlin.load.kotlin.nativeDeclarations.SuppressNoBodyErrorsForNativeDeclarations;
import org.jetbrains.kotlin.resolve.AnalyzerScriptParameter;
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticsWithSuppression;
import org.jetbrains.kotlin.resolve.jvm.diagnostics.DefaultErrorMessagesJvm;
import org.jetbrains.kotlin.utils.PathUtil;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.backend.BackendSettings;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EnvironmentManager {
    private static File KOTLIN_RUNTIME = initializeKotlinRuntime();
    private Getter<FileTypeRegistry> registry;
    private KotlinCoreEnvironment environment;
    private Disposable disposable = new Disposable() {
        @Override
        public void dispose() {
        }
    };

    @Nullable
    private static File initializeKotlinRuntime() {
        final File unpackedRuntimePath = getUnpackedRuntimePath();
        if (unpackedRuntimePath != null) {
            BackendSettings.KOTLIN_LIBS_DIR = unpackedRuntimePath.getParentFile().getAbsolutePath();
            ErrorWriter.writeInfoToConsole("Kotlin Runtime library founded at " + BackendSettings.KOTLIN_LIBS_DIR);
            return unpackedRuntimePath;
        } else {
            final File runtimeJarPath = getRuntimeJarPath();
            if (runtimeJarPath != null && runtimeJarPath.exists()) {
                BackendSettings.KOTLIN_LIBS_DIR = runtimeJarPath.getParentFile().getAbsolutePath();
                ErrorWriter.writeInfoToConsole("Kotlin Runtime library founded at " + BackendSettings.KOTLIN_LIBS_DIR);
                return runtimeJarPath;
            }
        }
        return null;
    }

    @Nullable
    private static File getUnpackedRuntimePath() {
        URL url = K2JVMCompiler.class.getClassLoader().getResource("jet/JetObject.class");
        if (url != null && url.getProtocol().equals("file")) {
            return new File(url.getPath()).getParentFile().getParentFile();
        }
        return null;
    }

    @Nullable
    private static File getRuntimeJarPath() {
        URL url = K2JVMCompiler.class.getClassLoader().getResource("kotlin/KotlinPackage.class");
        if (url != null && url.getProtocol().equals("jar")) {
            String path = url.getPath();
            return new File(path.substring(path.indexOf(":") + 1, path.indexOf("!/")));
        }
        return null;
    }

    @NotNull
    private static List<File> getClasspath(@NotNull K2JVMCompilerArguments arguments) {
        List<File> classpath = Lists.newArrayList();
        classpath.addAll(PathUtil.getJdkClassesRoots());

        Collection<File> files = Collections2.filter(PathUtil.getJdkClassesRoots(), new Predicate<File>() {
            @Override
            public boolean apply(@NotNull File file) {
                return file.getName().equals("rt.jar") || file.getName().endsWith("classes.jar");
            }
        });

        BackendSettings.JAVA_HOME = files.iterator().next().getParentFile().getParentFile().getParentFile().getAbsolutePath();

        File junit = new File(BackendSettings.LIBS_DIR + File.separator + "junit.jar");

        if (junit.exists()) {
            classpath.add(junit);
        } else {

        }

        classpath.add(KOTLIN_RUNTIME);
        classpath.add(new File(BackendSettings.KOTLIN_LIBS_DIR + File.separator + "kotlin-reflect.jar"));
        if (arguments.classpath != null) {
            for (String element : Splitter.on(File.pathSeparatorChar).split(arguments.classpath)) {
                classpath.add(new File(element));
            }
        }
        return classpath;
    }

    @NotNull
    public KotlinCoreEnvironment getEnvironment() {
        if (environment == null) {
            environment = createEnvironment();

            Extensions.getRootArea()
                    .getExtensionPoint(ClassFileDecompilers.EP_NAME)
                    .registerExtension(new JetClassFileDecompiler());

            //throw new IllegalStateException("Environment should be initialized before");
        }
        return environment;
    }

    public Disposable getDisposable() {
        return disposable;
    }

    public Getter<FileTypeRegistry> getRegistry() {
        return registry;
    }

    @NotNull
    private KotlinCoreEnvironment createEnvironment() {
        K2JVMCompilerArguments arguments = new K2JVMCompilerArguments();
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addAll(JVMConfigurationKeys.CLASSPATH_KEY, getClasspath(arguments));
        configuration.addAll(JVMConfigurationKeys.ANNOTATIONS_PATH_KEY, getAnnotationsPath());

        configuration.put(JVMConfigurationKeys.SCRIPT_PARAMETERS, Collections.<AnalyzerScriptParameter>emptyList());

        configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions);
        configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions);

        KotlinCoreEnvironment kotlinCoreEnvironment = KotlinCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);
        Extensions.getRootArea().getExtensionPoint(DefaultErrorMessages.Extension.EP_NAME).registerExtension(new DefaultErrorMessagesJvm());
        Extensions.getRootArea().getExtensionPoint(DefaultErrorMessages.Extension.EP_NAME).registerExtension(new DefaultErrorMessagesJs());
        Extensions.getRootArea().getExtensionPoint(DiagnosticsWithSuppression.SuppressStringProvider.EP_NAME).registerExtension(new SuppressNoBodyErrorsForNativeDeclarations());
        Extensions.getRootArea().getExtensionPoint(DiagnosticsWithSuppression.SuppressStringProvider.EP_NAME).registerExtension(new SuppressUnusedParameterForJsNative());
        registry = FileTypeRegistry.ourInstanceGetter;
        return kotlinCoreEnvironment;
    }

    @NotNull
    private List<File> getAnnotationsPath() {
        List<File> annotationsPath = Lists.newArrayList();
        annotationsPath.add(PathUtil.getKotlinPathsForCompiler().getJdkAnnotationsPath());
        return annotationsPath;
    }

}
