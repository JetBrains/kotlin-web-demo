/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.impl;

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
import org.jetbrains.kotlin.config.LanguageVersion;
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.container.ContainerKt;
import org.jetbrains.kotlin.container.DslKt;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.context.ContextKt;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.frontend.java.di.InjectionKt;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.js.config.JsConfig;
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig;
import org.jetbrains.kotlin.js.resolve.JsPlatform;
import org.jetbrains.kotlin.load.java.JavaClassFinderImpl;
import org.jetbrains.kotlin.load.java.lazy.LazyJavaPackageFragmentProvider;
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.*;
import org.jetbrains.kotlin.resolve.jvm.JavaClassFinderPostConstruct;
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolver;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform;
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl;
import org.jetbrains.kotlin.resolve.lazy.ResolveSession;
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import org.jetbrains.kotlin.serialization.js.JsModuleDescriptor;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.kotlin.cli.jvm.config.ModuleNameKt.getModuleName;

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
                ClassBuilderFactories.binaries(false),
                analyzeExhaust.getModuleDescriptor(),
                analyzeExhaust.getBindingContext(),
                files
        );
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFileForJvm(@NotNull List<KtFile> files, Project project) {
        KotlinCoreEnvironment environment = EnvironmentManager.getEnvironment();
        LanguageVersion languageVersion = EnvironmentManager.getLanguageVersion();
        ModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(project, getModuleName(environment));
        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), files);

        Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> analyzerAndProvider = createContainerForTopDownAnalyzerForJvm(
                moduleContext, trace, providerFactory,
                GlobalSearchScope.allScope(project), LookupTracker.Companion.getDO_NOTHING(), new JvmPackagePartProvider(EnvironmentManager.getEnvironment())
        );

        List<LazyJavaPackageFragmentProvider> additionalProviders = Collections.singletonList(
                DslKt.getService(analyzerAndProvider.second, JavaDescriptorResolver.class).getPackageFragmentProvider());

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
        StorageComponentContainer container = DslKt.createContainer("TopDownAnalyzerForJvm", new Function1<StorageComponentContainer, Unit>() {
            @Override
            public Unit invoke(StorageComponentContainer storageComponentContainer) {
                DslKt.useInstance(storageComponentContainer, packagePartProvider);

                org.jetbrains.kotlin.frontend.di.InjectionKt.configureModule(storageComponentContainer, moduleContext, JvmPlatform.INSTANCE, bindingTrace);
                InjectionKt.configureJavaTopDownAnalysis(storageComponentContainer, moduleContentScope, moduleContext.getProject(), lookupTracker, LanguageVersionSettingsImpl.DEFAULT);

                DslKt.useInstance(storageComponentContainer, declarationProviderFactory);

                CompilerEnvironment.INSTANCE.configure(storageComponentContainer);

                ContainerKt.registerSingleton(storageComponentContainer, SingleModuleClassResolver.class);
                return null;
            }
        });

        DslKt.getService(container, JavaClassFinderImpl.class).initialize();
        DslKt.getService(container, JavaClassFinderPostConstruct.class).postCreate();

        //noinspection unchecked
        return new Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider>(new ContainerForTopDownAnalyzerForJvm(container).getLazyTopDownAnalyzerForTopLevel(), container);
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFileForJs(@NotNull List<KtFile> files, Project project) {
        KotlinCoreEnvironment environment = EnvironmentManager.getEnvironment();
        LanguageVersion languageVersion = EnvironmentManager.getLanguageVersion();

        String moduleName = getModuleName(environment);
        JsConfig config = new LibrarySourcesConfig.Builder(
                project,
                moduleName,
                Collections.singletonList(WrapperSettings.JS_LIB_ROOT.toString())
        ).build();

        MutableModuleContext module = ContextKt.ContextForNewModule(
                project, Name.special("<" + moduleName + ">"), JsPlatform.INSTANCE
        );
        module.setDependencies(computeDependencies(module.getModule(), config));

        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(module.getStorageManager(), files);

        Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> analyzerAndProvider = createContainerForTopDownAnalyzerForJs(module, trace, providerFactory, languageVersion);

        analyzerAndProvider.getFirst().analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, files, Collections.<PackageFragmentProvider>emptyList());

        //noinspection unchecked
        return new Pair<AnalysisResult, ComponentProvider>(
                AnalysisResult.success(trace.getBindingContext(), module.getModule()),
                analyzerAndProvider.second);
    }

    @NotNull
    private static List<ModuleDescriptorImpl> computeDependencies(ModuleDescriptorImpl module, @NotNull JsConfig config) {
        List<ModuleDescriptorImpl> allDependencies = new ArrayList<ModuleDescriptorImpl>();
        allDependencies.add(module);
        for (JsModuleDescriptor<ModuleDescriptorImpl> descriptor : config.getModuleDescriptors()){
            allDependencies.add(descriptor.getData());
        }
        allDependencies.add(JsPlatform.INSTANCE.getBuiltIns().getBuiltInsModule());
        return allDependencies;
    }

    private static Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider> createContainerForTopDownAnalyzerForJs(
            final ModuleContext moduleContext,
            final BindingTrace bindingTrace,
            final DeclarationProviderFactory declarationProviderFactory,
            final LanguageVersion languageVersion
    ) {
        StorageComponentContainer container = DslKt.createContainer("TopDownAnalyzerForJs", new Function1<StorageComponentContainer, Unit>() {
            @Override
            public Unit invoke(StorageComponentContainer storageComponentContainer) {
                org.jetbrains.kotlin.frontend.di.InjectionKt.configureModule(storageComponentContainer, moduleContext, JsPlatform.INSTANCE, bindingTrace);

                DslKt.useInstance(storageComponentContainer, declarationProviderFactory);
                ContainerKt.registerSingleton(storageComponentContainer, FileScopeProviderImpl.class);

                CompilerEnvironment.INSTANCE.configure(storageComponentContainer);

                DslKt.useInstance(storageComponentContainer, LookupTracker.Companion.getDO_NOTHING());
                DslKt.useInstance(storageComponentContainer, languageVersion);
                ContainerKt.registerSingleton(storageComponentContainer, ResolveSession.class);
                ContainerKt.registerSingleton(storageComponentContainer, LazyTopDownAnalyzerForTopLevel.class);

                return null;
            }
        });

        //noinspection unchecked
        return new Pair<LazyTopDownAnalyzerForTopLevel, ComponentProvider>(DslKt.getService(container, LazyTopDownAnalyzerForTopLevel.class), container);
    }


}
