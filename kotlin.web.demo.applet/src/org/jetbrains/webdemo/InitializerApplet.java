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
import org.jetbrains.jet.config.CompilerConfiguration;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Getter;

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

            environment = new JetCoreEnvironment(root, new CompilerConfiguration());
            registry = FileTypeRegistry.ourInstanceGetter;

            return true;
        }
        return false;
    }
}


