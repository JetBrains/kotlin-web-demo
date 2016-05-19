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

package org.jetbrains.webdemo.kotlin.impl.environment;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.ContainerProvider;
import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.codeInsight.runner.JavaMainMethodProvider;
import com.intellij.core.CoreApplicationEnvironment;
import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.fileTypes.FileTypeExtensionPoint;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Getter;
import com.intellij.psi.FileContextProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.compiled.ClassFileDecompilers;
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy;
import com.intellij.psi.meta.MetaDataContributor;
import com.intellij.psi.stubs.BinaryFileStubBuilders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.cli.jvm.config.JvmContentRootsKt;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.js.analyze.SuppressUnusedParameterForJsNative;
import org.jetbrains.kotlin.js.resolve.diagnostics.DefaultErrorMessagesJs;
import org.jetbrains.kotlin.resolve.diagnostics.SuppressStringProvider;
import org.jetbrains.kotlin.resolve.jvm.diagnostics.DefaultErrorMessagesJvm;
import org.jetbrains.kotlin.utils.PathUtil;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.kotlin.idea.DummyCodeStyleManager;
import org.jetbrains.webdemo.kotlin.impl.WrapperSettings;

import java.io.File;
import java.util.List;

import static org.jetbrains.kotlin.cli.jvm.config.JVMConfigurationKeys.MODULE_NAME;

public class EnvironmentManager {
    private static Getter<FileTypeRegistry> registry;
    private static KotlinCoreEnvironment environment;
    private static Disposable disposable = new Disposable() {
        @Override
        public void dispose() {
        }
    };

    public static void init(){
        environment = createEnvironment();
    }

    public static void reinitializeJavaEnvironment(){
        ApplicationManager.setApplication(
                environment.getApplication(),
                registry,
                disposable
        );
    }

    @NotNull
    public static KotlinCoreEnvironment getEnvironment() {
        if (environment == null) {
            throw new IllegalStateException("Environment should be initialized before");
        }
        return environment;
    }

    @NotNull
    private static KotlinCoreEnvironment createEnvironment() {
        K2JVMCompilerArguments arguments = new K2JVMCompilerArguments();
        CompilerConfiguration configuration = new CompilerConfiguration();

        JvmContentRootsKt.addJvmClasspathRoots(configuration, getClasspath(arguments));

        configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions);
        configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions);
        configuration.put(MODULE_NAME, "kotlinWebDemo");

        KotlinCoreEnvironment environment = KotlinCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);
        ((MockProject) environment.getProject()).registerService(NullableNotNullManager.class, new NullableNotNullManager() {
            @Override
            public boolean isNullable(@NotNull PsiModifierListOwner owner, boolean checkBases) {
                return false;
            }

            @Override
            public boolean isNotNull(@NotNull PsiModifierListOwner owner, boolean checkBases) {
                return true;
            }

            @Override
            protected boolean hasHardcodedContracts(PsiElement element) {
                return false;
            }
        });
        ((MockProject) environment.getProject()).registerService(CodeStyleManager.class, new DummyCodeStyleManager());


        Extensions.getRootArea().getExtensionPoint(DefaultErrorMessages.Extension.EP_NAME).registerExtension(new DefaultErrorMessagesJvm());
        Extensions.getRootArea().getExtensionPoint(DefaultErrorMessages.Extension.EP_NAME).registerExtension(new DefaultErrorMessagesJs());
        Extensions.getRootArea().getExtensionPoint(SuppressStringProvider.Companion.getEP_NAME()).registerExtension(new SuppressUnusedParameterForJsNative());

        registerExtensionPoints(Extensions.getRootArea());

        registry = FileTypeRegistry.ourInstanceGetter;
        return environment;
    }

    private static void registerExtensionPoints(ExtensionsArea area) {
        CoreApplicationEnvironment.registerExtensionPoint(area, BinaryFileStubBuilders.EP_NAME, FileTypeExtensionPoint.class);
        CoreApplicationEnvironment.registerExtensionPoint(area, FileContextProvider.EP_NAME, FileContextProvider.class);

        CoreApplicationEnvironment.registerExtensionPoint(area, MetaDataContributor.EP_NAME, MetaDataContributor.class);
        CoreApplicationEnvironment.registerExtensionPoint(area, PsiAugmentProvider.EP_NAME, PsiAugmentProvider.class);
        CoreApplicationEnvironment.registerExtensionPoint(area, JavaMainMethodProvider.EP_NAME, JavaMainMethodProvider.class);

        CoreApplicationEnvironment.registerExtensionPoint(area, ContainerProvider.EP_NAME, ContainerProvider.class);
        CoreApplicationEnvironment.registerExtensionPoint(area, ClsCustomNavigationPolicy.EP_NAME, ClsCustomNavigationPolicy.class);
        CoreApplicationEnvironment.registerExtensionPoint(area, ClassFileDecompilers.EP_NAME, ClassFileDecompilers.Decompiler.class);
    }

    @NotNull
    private static List<File> getClasspath(@NotNull K2JVMCompilerArguments arguments) {
        List<File> classpath = Lists.newArrayList();
        classpath.addAll(PathUtil.getJdkClassesRoots());

        File junit = new File(BackendSettings.LIBS_DIR + File.separator + "junit.jar");

        if (junit.exists()) {
            classpath.add(junit);
        }

        classpath.add(new File(WrapperSettings.KOTLIN_JARS_FOLDER + File.separator + "kotlin-runtime.jar"));
        classpath.add(new File(WrapperSettings.KOTLIN_JARS_FOLDER + File.separator + "kotlin-reflect.jar"));
        classpath.add(new File(WrapperSettings.KOTLIN_JARS_FOLDER + File.separator + "kotlin-test.jar"));
        if (arguments.classpath != null) {
            for (String element : Splitter.on(File.pathSeparatorChar).split(arguments.classpath)) {
                classpath.add(new File(element));
            }
        }
        return classpath;
    }
}
