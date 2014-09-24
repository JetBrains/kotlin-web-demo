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

import com.google.common.base.Predicates;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.analyzer.AnalyzeExhaust;
import org.jetbrains.jet.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.jet.codegen.ClassBuilderFactories;
import org.jetbrains.jet.codegen.state.GenerationState;
import org.jetbrains.jet.lang.descriptors.impl.ModuleDescriptorImpl;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.BindingTraceContext;
import org.jetbrains.jet.lang.resolve.java.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import java.util.Collections;

public class ResolveUtils {

    public static BindingContext getBindingContext(@NotNull JetFile file) {
        AnalyzeExhaust analyzeExhaust = analyzeFile(file);
        return analyzeExhaust.getBindingContext();
    }

    public static GenerationState getGenerationState(@NotNull JetFile file) {
        AnalyzeExhaust analyzeExhaust = analyzeFile(file);
        return new GenerationState(
                file.getProject(),
                ClassBuilderFactories.BINARIES,
                analyzeExhaust.getModuleDescriptor(),
                analyzeExhaust.getBindingContext(),
                Collections.singletonList(file)
        );
    }

    private static AnalyzeExhaust analyzeFile(JetFile file) {
        Project project = file.getProject();

        BindingTraceContext bindingTraceContext = new BindingTraceContext();

        ModuleDescriptorImpl module = TopDownAnalyzerFacadeForJVM.createJavaModule("<module>");
        module.addDependencyOnModule(module);
        module.addDependencyOnModule(KotlinBuiltIns.getInstance().getBuiltInsModule());
        module.seal();
        CliLightClassGenerationSupport lightClassGenerationSupport = CliLightClassGenerationSupport.getInstanceForCli(project);
        if (lightClassGenerationSupport != null) {
            lightClassGenerationSupport.setModule(module);
        }
        return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                project,
                Collections.singleton(file),
                bindingTraceContext,
                Predicates.<PsiFile>alwaysTrue(),
                module, null, null);
    }

    private ResolveUtils() {
    }
}
