package org.jetbrains.webdemo.environment;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.jet.cli.jvm.JVMConfigurationKeys;
import org.jetbrains.jet.cli.jvm.K2JVMCompiler;
import org.jetbrains.jet.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.utils.PathUtil;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EnvironmentManagerForServer extends EnvironmentManager {
    private static File KOTLIN_RUNTIME = initializeKotlinRuntime();

    @Nullable
    private static File initializeKotlinRuntime() {
        final File unpackedRuntimePath = getUnpackedRuntimePath();
        if (unpackedRuntimePath != null) {
            ApplicationSettings.KOTLIN_LIB = unpackedRuntimePath.getAbsolutePath();
            ErrorWriter.writeInfoToConsole("Kotlin Runtime library founded at " + ApplicationSettings.KOTLIN_LIB);
            return unpackedRuntimePath;
        } else {
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

        ApplicationSettings.JAVA_HOME = files.iterator().next().getParentFile().getParentFile().getParentFile().getAbsolutePath();

        File junit = new File(ApplicationSettings.LIBS_DIR + "junit.jar");

        if (junit.exists()) {
            classpath.add(junit);
        } else {

        }

        classpath.add(KOTLIN_RUNTIME);
        if (arguments.classpath != null) {
            for (String element : Splitter.on(File.pathSeparatorChar).split(arguments.classpath)) {
                classpath.add(new File(element));
            }
        }
        return classpath;
    }

    @NotNull
    public JetCoreEnvironment createEnvironment() {
        K2JVMCompilerArguments arguments = new K2JVMCompilerArguments();
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addAll(JVMConfigurationKeys.CLASSPATH_KEY, getClasspath(arguments));
        configuration.addAll(JVMConfigurationKeys.ANNOTATIONS_PATH_KEY, getAnnotationsPath());

        configuration.put(JVMConfigurationKeys.SCRIPT_PARAMETERS, Collections.<AnalyzerScriptParameter>emptyList());

        configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions);
        configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions);

        JetCoreEnvironment jetCoreEnvironment = JetCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);
        registry = FileTypeRegistry.ourInstanceGetter;
        return jetCoreEnvironment;
    }

    @NotNull
    private List<File> getAnnotationsPath() {
        List<File> annotationsPath = Lists.newArrayList();
        annotationsPath.add(PathUtil.getKotlinPathsForCompiler().getJdkAnnotationsPath());
        return annotationsPath;
    }
}
