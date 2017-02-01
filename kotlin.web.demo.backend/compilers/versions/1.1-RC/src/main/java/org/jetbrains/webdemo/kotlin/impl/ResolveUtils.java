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
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.codegen.ClassBuilderFactories;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.container.ContainerKt;
import org.jetbrains.kotlin.container.DslKt;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.context.ContextKt;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.ModuleDescriptor;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS;
import org.jetbrains.kotlin.js.config.JSConfigurationKeys;
import org.jetbrains.kotlin.js.config.JsConfig;
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig;
import org.jetbrains.kotlin.js.resolve.JsPlatform;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.CompilerEnvironment;
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension;
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl;
import org.jetbrains.kotlin.resolve.lazy.KotlinCodeAnalyzer;
import org.jetbrains.kotlin.resolve.lazy.ResolveSession;
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import org.jetbrains.kotlin.serialization.js.JsModuleDescriptor;
import org.jetbrains.kotlin.storage.StorageManager;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResolveUtils {

    private ResolveUtils() {
    }

    public static BindingContext getBindingContext(@NotNull List<KtFile> files, Project project, boolean isJs) {
        Pair<AnalysisResult, ComponentProvider> result = isJs ? analyzeFileForJs(files, project) : analyzeFileForJvm(files, project);
        AnalysisResult analyzeExhaust = result.getFirst();
        return analyzeExhaust.getBindingContext();
    }

    public static GenerationState getGenerationState(@NotNull List<KtFile> files, Project project, CompilerConfiguration compilerConfiguration) {
        AnalysisResult analyzeExhaust = analyzeFileForJvm(files, project).getFirst();
        return new GenerationState(
                project,
                ClassBuilderFactories.binaries(false),
                analyzeExhaust.getModuleDescriptor(),
                analyzeExhaust.getBindingContext(),
                files,
                compilerConfiguration
        );
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFileForJvm(@NotNull List<KtFile> files, Project project) {
        KotlinCoreEnvironment environment = EnvironmentManager.getEnvironment();
        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();


        CompilerConfiguration configuration = environment.getConfiguration();
        configuration.put(JVMConfigurationKeys.ADD_BUILT_INS_FROM_COMPILER_TO_DEPENDENCIES, true);
        ComponentProvider container = TopDownAnalyzerFacadeForJVM.INSTANCE.createContainer(
                environment.getProject(),
                files,
                trace,
                configuration,
                new Function1<GlobalSearchScope, PackagePartProvider>() {
                    @Override
                    public PackagePartProvider invoke(GlobalSearchScope globalSearchScope) {
                        return new JvmPackagePartProvider(EnvironmentManager.getEnvironment(), globalSearchScope);
                    }
                },
                new Function2<StorageManager, Collection<? extends KtFile>, DeclarationProviderFactory>() {
                    @Override
                    public DeclarationProviderFactory invoke(StorageManager storageManager, Collection<? extends KtFile> ktFiles) {
                        return new FileBasedDeclarationProviderFactory(storageManager, (Collection<KtFile>) ktFiles);
                    }
                },
                TopDownAnalyzerFacadeForJVM.INSTANCE.newModuleSearchScope(project, files)
        );

        DslKt.getService(container, LazyTopDownAnalyzer.class).analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, files, DataFlowInfo.Companion.getEMPTY());

        ModuleDescriptor moduleDescriptor = DslKt.getService(container, ModuleDescriptor.class);
        for (AnalysisHandlerExtension extension : AnalysisHandlerExtension.Companion.getInstances(project)) {
            AnalysisResult result = extension.analysisCompleted(project, moduleDescriptor, trace, files);
            if (result != null) break;
        }

        //noinspection unchecked
        return new Pair<AnalysisResult, ComponentProvider>(
                AnalysisResult.success(trace.getBindingContext(), moduleDescriptor),
                container);
    }

    public static Pair<AnalysisResult, ComponentProvider> analyzeFileForJs(@NotNull List<KtFile> files, Project project) {
        KotlinCoreEnvironment environment = EnvironmentManager.getEnvironment();

        CompilerConfiguration configuration = environment.getConfiguration().copy();
        configuration.put(JSConfigurationKeys.LIBRARY_FILES, Collections.singletonList(WrapperSettings.JS_LIB_ROOT.toString()));
        JsConfig config = new LibrarySourcesConfig(project, configuration);

        MutableModuleContext module = ContextKt.ContextForNewModule(
                ContextKt.ProjectContext(project),
                Name.special("<" + config.getModuleId() + ">"),
                JsPlatform.INSTANCE.getBuiltIns(),
                null
        );
        module.setDependencies(computeDependencies(module.getModule(), config));

        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(module.getStorageManager(), files);

        Pair<LazyTopDownAnalyzer, ComponentProvider> analyzerAndProvider = createContainerForTopDownAnalyzerForJs(module, trace, providerFactory);

        //noinspection unchecked
        return new Pair<AnalysisResult, ComponentProvider>(
                TopDownAnalyzerFacadeForJS.analyzeFiles(files, config),
                analyzerAndProvider.second);
    }

    @NotNull
    private static List<ModuleDescriptorImpl> computeDependencies(ModuleDescriptorImpl module, @NotNull JsConfig config) {
        List<ModuleDescriptorImpl> allDependencies = new ArrayList<ModuleDescriptorImpl>();
        allDependencies.add(module);
        for (JsModuleDescriptor<ModuleDescriptorImpl> jsModuleDescriptor : config.getModuleDescriptors()) {
            allDependencies.add(jsModuleDescriptor.getData());
        }
        allDependencies.add(JsPlatform.INSTANCE.getBuiltIns().getBuiltInsModule());
        return allDependencies;
    }

    private static Pair<LazyTopDownAnalyzer, ComponentProvider> createContainerForTopDownAnalyzerForJs(
            final ModuleContext moduleContext,
            final BindingTrace bindingTrace,
            final DeclarationProviderFactory declarationProviderFactory
    ) {
        StorageComponentContainer container = DslKt.composeContainer(
                "TopDownAnalyzerForJs",
                JsPlatform.INSTANCE.getPlatformConfigurator().getPlatformSpecificContainer(),
                new Function1<StorageComponentContainer, Unit>() {
            @Override
            public Unit invoke(StorageComponentContainer storageComponentContainer) {
                org.jetbrains.kotlin.frontend.di.InjectionKt.configureModule(storageComponentContainer, moduleContext, JsPlatform.INSTANCE, bindingTrace);

                DslKt.useInstance(storageComponentContainer, declarationProviderFactory);
                ContainerKt.registerSingleton(storageComponentContainer, FileScopeProviderImpl.class);

                CompilerEnvironment.INSTANCE.configure(storageComponentContainer);

                DslKt.useInstance(storageComponentContainer, LookupTracker.Companion.getDO_NOTHING());
                DslKt.useInstance(storageComponentContainer, LanguageVersionSettingsImpl.DEFAULT);
                ContainerKt.registerSingleton(storageComponentContainer, ResolveSession.class);
                ContainerKt.registerSingleton(storageComponentContainer, LazyTopDownAnalyzer.class);

                return null;
            }
        });

        DslKt.getService(container, ModuleDescriptorImpl.class).initialize(DslKt.getService(container, KotlinCodeAnalyzer.class).getPackageFragmentProvider());

        //noinspection unchecked
        return new Pair<LazyTopDownAnalyzer, ComponentProvider>(DslKt.getService(container, LazyTopDownAnalyzer.class), container);
    }


}
