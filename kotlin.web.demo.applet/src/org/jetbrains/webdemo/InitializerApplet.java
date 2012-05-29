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

import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.internal.com.intellij.openapi.Disposable;
import org.jetbrains.jet.internal.com.intellij.openapi.fileTypes.FileTypeRegistry;
import org.jetbrains.jet.internal.com.intellij.openapi.util.Getter;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;
import org.jetbrains.jet.plugin.JetFileType;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/14/11
 * Time: 3:49 PM
 */
public class InitializerApplet extends Initializer {
    private static InitializerApplet initializer = new InitializerApplet();

    public static InitializerApplet getInstance() {
        return initializer;
    }

    private InitializerApplet() {
    }

    private static JetCoreEnvironment environment;

    public JetCoreEnvironment getEnvironment() {
        if (environment != null) {
            return environment;
        }
        return null;
    }

    private static Getter<FileTypeRegistry> registry;
    private static Disposable root;

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
            environment = new JetCoreEnvironment(root,
                    CompilerDependencies.compilerDependenciesForProduction(CompilerSpecialMode.APPLET_WEB_DEMO));

            environment.registerFileType(JetFileType.INSTANCE, "kt");
            environment.registerFileType(JetFileType.INSTANCE, "kts");
            environment.registerFileType(JetFileType.INSTANCE, "ktm");
            environment.registerFileType(JetFileType.INSTANCE, "jet");
            environment.registerParserDefinition(new JetParserDefinition());

            registry = FileTypeRegistry.ourInstanceGetter;

            return true;
            //return setJavaCoreEnvironment();
        }
        return false;
    }
}


