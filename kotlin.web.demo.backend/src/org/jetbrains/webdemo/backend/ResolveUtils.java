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
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.codegen.ClassBuilderFactories;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.container.ContainerPackage;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.context.ContextPackage;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.frontend.java.di.DiPackage;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS;
import org.jetbrains.kotlin.js.config.Config;
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig;
import org.jetbrains.kotlin.js.resolve.JsPlatform;
import org.jetbrains.kotlin.load.java.lazy.LazyJavaPackageFragmentProvider;
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.*;
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolver;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform;
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl;
import org.jetbrains.kotlin.resolve.lazy.ResolveSession;
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import org.jetbrains.webdemo.backend.translator.WebDemoTranslatorFacade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.kotlin.cli.jvm.config.ConfigPackage.getModuleName;

public class ResolveUtils {

    private ResolveUtils() {
    }

    public static BindingContext getBindingContext(@NotNull List<KtFile> files, Project project, boolean isJs) {
        Pair<AnalysisResult, ComponentProvider> result = isJs ? analyzeFileForJs(files, project) : analyzeFileForJvm(files, project);
        AnalysisResult analyzeExhaust = result.getFirst();
        return analyzeExhaust.getBindingContext();
    }

    public static GenerationState getGenerationState(@NotNull List<KtFile> files, Project project) {
        AnalysisResult analyzeExhaust = analyzeFileForJvm(files, project).getFirst();
        return new GenerationState(
                project,
                ClassBuilderFactories.BINARIES,
                analyzeExhaust.getModuleDescriptor(),
                analyzeExhaust.getBindingContext(),
                files
        );
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFileForJvm(@NotNull List<KtFile> files, Project project) {

        KotlinCoreEnvironment environment = Initializer.getInstance().getEnvironment();
        ModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(project, getModuleName(environment));
        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), files);

        Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> analyzerAndProvider = createContainerForTopDownAnalyzerForJvm(
                moduleContext, trace, providerFactory,
                GlobalSearchScope.allScope(project), LookupTracker.DO_NOTHING, new JvmPackagePartProvider(Initializer.getInstance().getEnvironment()));

        List<LazyJavaPackageFragmentProvider> additionalProviders = Collections.singletonList(
                ContainerPackage.getService(analyzerAndProvider.second, JavaDescriptorResolver.class).getPackageFragmentProvider());

        analyzerAndProvider.first.analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, files, additionalProviders);

        //noinspection unchecked
        return new Pair<AnalysisResult, ComponentProvider>(
                AnalysisResult.success(trace.getBindingContext(), moduleContext.getModule()),
                analyzerAndProvider.second);
    }

    private static Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> createContainerForTopDownAnalyzerForJvm(
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
        return new Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider>(new ContainerForTopDownAnalyzerForJvm(container).getLazyTopDownAnalyzerForTopLevel(), container);
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFileForJs(@NotNull List<KtFile> files, Project project) {
        KotlinCoreEnvironment environment = Initializer.getInstance().getEnvironment();

        String moduleName = getModuleName(environment);
        Config config = new LibrarySourcesConfig.Builder(
                project,
                moduleName,
                WebDemoTranslatorFacade.LIBRARY_FILES
        ).build();

        MutableModuleContext module = ContextPackage.ContextForNewModule(
                project, Name.special("<" + moduleName + ">"), TopDownAnalyzerFacadeForJS.JS_MODULE_PARAMETERS
        );
        module.setDependencies(computeDependencies(module.getModule(), config));

        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(module.getStorageManager(), files);

        Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> analyzerAndProvider = createContainerForTopDownAnalyzerForJs(module, trace, providerFactory);

        analyzerAndProvider.getFirst().analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, files, Collections.<PackageFragmentProvider>emptyList());

        //noinspection unchecked
        return new Pair<AnalysisResult, ComponentProvider>(
                AnalysisResult.success(trace.getBindingContext(), module.getModule()),
                analyzerAndProvider.second);
    }

    @NotNull
    private static List<ModuleDescriptorImpl> computeDependencies(ModuleDescriptorImpl module, @NotNull Config config) {
        List<ModuleDescriptorImpl> allDependencies = new ArrayList<ModuleDescriptorImpl>();
        allDependencies.add(module);
        allDependencies.addAll(config.getModuleDescriptors());
        allDependencies.add(KotlinBuiltIns.getInstance().getBuiltInsModule());
        return allDependencies;
    }

    private static Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> createContainerForTopDownAnalyzerForJs(
            final ModuleContext moduleContext,
            final BindingTrace bindingTrace,
            final DeclarationProviderFactory declarationProviderFactory
    ) {
        StorageComponentContainer container = ContainerPackage.createContainer("TopDownAnalyzerForJs", new Function1<StorageComponentContainer, Unit>() {
            @Override
            public Unit invoke(StorageComponentContainer storageComponentContainer) {
                org.jetbrains.kotlin.frontend.di.DiPackage.configureModule(storageComponentContainer, moduleContext, JsPlatform.INSTANCE$, bindingTrace);

                ContainerPackage.useInstance(storageComponentContainer, declarationProviderFactory);
                ContainerPackage.registerSingleton(storageComponentContainer, FileScopeProviderImpl.class);

                CompilerEnvironment.INSTANCE$.configure(storageComponentContainer);

                ContainerPackage.useInstance(storageComponentContainer, LookupTracker.DO_NOTHING);
                ContainerPackage.registerSingleton(storageComponentContainer, ResolveSession.class);
                ContainerPackage.registerSingleton(storageComponentContainer, LazyTopDownAnalyzerForTopLevel.class);

                return null;
            }
        });

        //noinspection unchecked
        return new Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider>(ContainerPackage.getService(container, LazyTopDownAnalyzerForTopLevel.class), container);
    }


}
