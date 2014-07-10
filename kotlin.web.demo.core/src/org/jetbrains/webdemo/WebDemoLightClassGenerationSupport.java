/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.jet.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.BindingTraceContext;

public class WebDemoLightClassGenerationSupport extends CliLightClassGenerationSupport {

    private BindingTrace trace;

    public static CliLightClassGenerationSupport getInstanceForCli(@NotNull Project project) {
        return ServiceManager.getService(project, WebDemoLightClassGenerationSupport.class);
    }

    @NotNull
    @Override
    public BindingTrace getTrace() {
        if (trace == null) {
            trace = new BindingTraceContext();
        }
        return trace;
    }

    @TestOnly
    public void newBindingTrace() {
        trace = null;
        super.newBindingTrace();
    }
}
