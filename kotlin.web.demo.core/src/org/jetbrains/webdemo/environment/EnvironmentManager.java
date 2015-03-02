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

package org.jetbrains.webdemo.environment;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Getter;
import com.intellij.psi.compiled.ClassFileDecompilers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.kotlin.idea.decompiler.JetClassFileDecompiler;

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

}
