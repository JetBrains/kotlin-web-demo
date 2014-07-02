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
import org.jetbrains.jet.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.jet.codegen.ClassBuilderFactories;
import org.jetbrains.jet.codegen.state.GenerationState;
import org.jetbrains.jet.context.ContextPackage;
import org.jetbrains.jet.context.GlobalContextImpl;
import org.jetbrains.jet.di.InjectorForTopDownAnalyzerForJvm;
import org.jetbrains.jet.lang.descriptors.DependencyKind;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptorImpl;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisParameters;

import java.util.Collections;

public class ResolveUtils {

    public static BindingContext getBindingContext(@NotNull JetFile file) {
        Project project = file.getProject();

        GlobalContextImpl globalContext = ContextPackage.GlobalContext();
        TopDownAnalysisParameters params = TopDownAnalysisParameters.create(
                globalContext.getStorageManager(), globalContext.getExceptionTracker(), Predicates.<PsiFile>alwaysTrue(), false, false);
        CliLightClassGenerationSupport support = CliLightClassGenerationSupport.getInstanceForCli(project);

        CliLightClassGenerationSupport.getInstanceForCli(project).newBindingTrace();
        BindingTrace sharedTrace = support.getTrace();

        ModuleDescriptorImpl sharedModule = support.getModule();

        InjectorForTopDownAnalyzerForJvm injector = new InjectorForTopDownAnalyzerForJvm(project, params, sharedTrace, sharedModule);
        sharedModule.addFragmentProvider(DependencyKind.BINARIES, injector.getJavaDescriptorResolver().getPackageFragmentProvider());
        injector.getTopDownAnalyzer().analyzeFiles(params, Collections.singleton(file));

        return sharedTrace.getBindingContext();
    }

    public static GenerationState getGenerationState(@NotNull JetFile file) {
        return new GenerationState(
                file.getProject(),
                ClassBuilderFactories.BINARIES,
                CliLightClassGenerationSupport.getInstanceForCli(file.getProject()).getModule(),
                getBindingContext(file),
                Collections.singletonList(file)
        );
    }

    private ResolveUtils() {
    }
}
