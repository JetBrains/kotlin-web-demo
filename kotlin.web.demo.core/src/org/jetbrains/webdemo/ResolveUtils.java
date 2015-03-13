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
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.codegen.ClassBuilderFactories;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.context.ContextPackage;
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;

import java.util.Collections;

public class ResolveUtils {

    public static BindingContext getBindingContext(@NotNull JetFile file) {
        AnalysisResult analyzeExhaust = analyzeFile(file);
        return analyzeExhaust.getBindingContext();
    }

    public static GenerationState getGenerationState(@NotNull JetFile file) {
        AnalysisResult analyzeExhaust = analyzeFile(file);
        return new GenerationState(
                file.getProject(),
                ClassBuilderFactories.BINARIES,
                analyzeExhaust.getModuleDescriptor(),
                analyzeExhaust.getBindingContext(),
                Collections.singletonList(file)
        );
    }

    private static AnalysisResult analyzeFile(JetFile file) {
        Project project = file.getProject();

        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        ModuleDescriptorImpl module = TopDownAnalyzerFacadeForJVM.createJavaModule("<module>");
        module.addDependencyOnModule(module);
        module.addDependencyOnModule(KotlinBuiltIns.getInstance().getBuiltInsModule());
        module.seal();

        return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationWithCustomContext(
                project, ContextPackage.GlobalContext(), Collections.singleton(file), trace, module, null, null);
    }

    private ResolveUtils() {
    }
}
