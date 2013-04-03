package org.jetbrains.webdemo.environment;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.cli.jvm.JVMConfigurationKeys;
import org.jetbrains.jet.cli.jvm.K2JVMCompiler;
import org.jetbrains.jet.cli.jvm.K2JVMCompilerArguments;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.codegen.BuiltinToJavaTypesMapping;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.utils.PathUtil;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public abstract class EnvironmentManager {

    protected Getter<FileTypeRegistry> registry;
    protected JetCoreEnvironment environment;
    protected Disposable disposable = new Disposable() {
        @Override
        public void dispose() {
        }
    };

    @NotNull
    protected abstract JetCoreEnvironment createEnvironment();

    @NotNull
    public JetCoreEnvironment getEnvironment() {
        if (environment == null) {
            environment = createEnvironment();
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
}
