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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.webdemo.environment.EnvironmentManager;

public abstract class Initializer {

    public static Initializer INITIALIZER;

    public abstract EnvironmentManager getEnvironmentManager();

    public static void reinitializeJavaEnvironment() {
        ApplicationManager.setApplication(INITIALIZER.getEnvironmentManager().getEnvironment().getApplication(),
                INITIALIZER.getEnvironmentManager().getRegistry(),
                INITIALIZER.getEnvironmentManager().getDisposable());
    }


    public KotlinCoreEnvironment getEnvironment() {
        return getEnvironmentManager().getEnvironment();
    }
}
