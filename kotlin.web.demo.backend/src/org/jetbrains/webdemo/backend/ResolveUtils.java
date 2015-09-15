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

package org.jetbrains.webdemo.backend;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.search.GlobalSearchScope;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.codegen.ClassBuilderFactories;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.container.ContainerPackage;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.frontend.java.di.DiPackage;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.load.java.lazy.LazyJavaPackageFragmentProvider;
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.CompilerEnvironment;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform;
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl;
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;

import java.util.Collections;
import java.util.List;

import static org.jetbrains.kotlin.cli.jvm.config.ConfigPackage.getModuleName;

public class ResolveUtils {

    private ResolveUtils() {
    }

    public static BindingContext getBindingContext(@NotNull List<JetFile> files, Project project) {
        AnalysisResult analyzeExhaust = analyzeFile(files, project).getFirst();
        return analyzeExhaust.getBindingContext();
    }

    public static GenerationState getGenerationState(@NotNull List<JetFile> files, Project project) {
        AnalysisResult analyzeExhaust = analyzeFile(files, project).getFirst();
        return new GenerationState(
                project,
                ClassBuilderFactories.BINARIES,
                analyzeExhaust.getModuleDescriptor(),
                analyzeExhaust.getBindingContext(),
                files
        );
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFile(@NotNull List<JetFile> files, Project project) {

        KotlinCoreEnvironment environment = Initializer.getInstance().getEnvironment();
        ModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(project, getModuleName(environment));
        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), files);

        Pair<ContainerForTopDownAnalyzerForJvm, ComponentProvider> containerAndProvider = createContainerForTopDownAnalyzerForJvm(
                moduleContext, trace, providerFactory,
                GlobalSearchScope.allScope(project), LookupTracker.DO_NOTHING, new JvmPackagePartProvider(Initializer.getInstance().getEnvironment()));
        ContainerForTopDownAnalyzerForJvm container = containerAndProvider.first;
        List<LazyJavaPackageFragmentProvider> additionalProviders = Collections.singletonList(container.getJavaDescriptorResolver().getPackageFragmentProvider());

        container.getLazyTopDownAnalyzerForTopLevel().analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, files, additionalProviders);

        //noinspection unchecked
        return new Pair<AnalysisResult, ComponentProvider>(
                AnalysisResult.success(trace.getBindingContext(), moduleContext.getModule()),
                containerAndProvider.second);
    }

    public static Pair<ContainerForTopDownAnalyzerForJvm, ComponentProvider> createContainerForTopDownAnalyzerForJvm(
            final ModuleContext moduleContext,
            final BindingTrace bindingTrace,
            final DeclarationProviderFactory declarationProviderFactory,
            final GlobalSearchScope moduleContentScope,
            final LookupTracker lookupTracker,
            final PackagePartProvider packagePartProvider
    ) {
        StorageComponentContainer container = ContainerPackage.createContainer("TopDownAnalyzerForJvm", new Function1<StorageComponentContainer, Unit>() {
            @Override
            public Unit invoke(StorageComponentContainer storageComponentContainer) {
                ContainerPackage.useInstance(storageComponentContainer, packagePartProvider);

                org.jetbrains.kotlin.frontend.di.DiPackage.configureModule(storageComponentContainer, moduleContext, JvmPlatform.INSTANCE$, bindingTrace);
                DiPackage.configureJavaTopDownAnalysis(storageComponentContainer, moduleContentScope, moduleContext.getProject(), lookupTracker);

                ContainerPackage.useInstance(storageComponentContainer, declarationProviderFactory);

                CompilerEnvironment.INSTANCE$.configure(storageComponentContainer);

                ContainerPackage.registerSingleton(storageComponentContainer, SingleModuleClassResolver.class);
                ContainerPackage.registerSingleton(storageComponentContainer, FileScopeProviderImpl.class);

                return null;
            }
        });

        DiPackage.javaAnalysisInit(container);

        //noinspection unchecked
        return new Pair<ContainerForTopDownAnalyzerForJvm, ComponentProvider>(new ContainerForTopDownAnalyzerForJvm(container), container);
    }


}
