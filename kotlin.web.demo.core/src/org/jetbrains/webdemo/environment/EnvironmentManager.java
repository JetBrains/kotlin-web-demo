package org.jetbrains.webdemo.environment;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;

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
