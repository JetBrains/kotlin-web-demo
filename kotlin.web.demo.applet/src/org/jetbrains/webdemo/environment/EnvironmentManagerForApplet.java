package org.jetbrains.webdemo.environment;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.webdemo.environment.EnvironmentManager;

public class EnvironmentManagerForApplet extends EnvironmentManager {

    @NotNull
    @Override
    public JetCoreEnvironment createEnvironment() {
        JetCoreEnvironment jetCoreEnvironment = new JetCoreEnvironment(getDisposable(), new CompilerConfiguration());
        registry = FileTypeRegistry.ourInstanceGetter;
        return jetCoreEnvironment;
    }
}
