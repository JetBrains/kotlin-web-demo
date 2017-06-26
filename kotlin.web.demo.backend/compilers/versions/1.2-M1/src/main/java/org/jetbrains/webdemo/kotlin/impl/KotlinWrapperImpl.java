/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.webdemo.KotlinVersionConfig;
import org.jetbrains.webdemo.kotlin.KotlinWrapper;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;
import org.jetbrains.webdemo.kotlin.datastructures.*;
import org.jetbrains.webdemo.kotlin.impl.analyzer.ErrorAnalyzer;
import org.jetbrains.webdemo.kotlin.impl.compiler.KotlinCompilerWrapper;
import org.jetbrains.webdemo.kotlin.impl.completion.CompletionProvider;
import org.jetbrains.webdemo.kotlin.impl.converter.WebDemoJavaToKotlinConverter;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;
import org.jetbrains.webdemo.kotlin.impl.translator.WebDemoTranslatorFacade;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KotlinWrapperImpl implements KotlinWrapper {
    private Path jarsFolder;
    private String kotlinVersion;
    private String kotlinBuild;
    private Path wrapperFolder;
    private String stdlibVersion;

    @Override
    public void init(List<Path> javaLibraries, KotlinVersionConfig config) {
        this.kotlinVersion = config.getVersion();
        this.kotlinBuild = config.getBuild();
        stdlibVersion = config.getStdlibVersion();
        WrapperLogger.init(kotlinVersion);
        wrapperFolder = KotlinWrappersManager.INSTANCE.getWrappersDir().resolve(kotlinVersion);
        jarsFolder = wrapperFolder.resolve("kotlin");
        WrapperSettings.JS_LIB_ROOT = wrapperFolder.resolve("js");
        List<Path> libraries = getKotlinRuntimeLibraries();
        libraries.addAll(javaLibraries);
        EnvironmentManager.init(libraries);
    }

    public String translateJavaToKotlin(String javaCode) {
        return WebDemoJavaToKotlinConverter.getResult(javaCode);
    }

    @Override
    public Map<String, List<ErrorDescriptor>> getErrors(Map<String, String> files, boolean isJs) {
        List<KtFile> psiFiles = createPsiFiles(files);
        ErrorAnalyzer analyzer = new ErrorAnalyzer(psiFiles, EnvironmentManager.getEnvironment().getProject());
        return analyzer.getAllErrors(isJs);
    }

    @Override
    public TranslationResult compileKotlinToJS(Map<String, String> files, String[] args) {
        List<KtFile> ktFiles = createPsiFiles(files);
        return WebDemoTranslatorFacade.translateProjectWithCallToMain(ktFiles, args);
    }

    @Override
    public List<CompletionVariant> getCompletionVariants(
            Map<String, String> projectFiles, String filename, int line, int ch, boolean isJs) {
        List<KtFile> files = createPsiFiles(projectFiles);
        CompletionProvider completionProvider = new CompletionProvider(files, filename, line, ch);
        return completionProvider.getResult(isJs);
    }

    @Override
    public CompilationResult compileCorrectFiles(Map<String, String> projectFiles, String fileName, boolean searchForMain) {
        List<KtFile> files = createPsiFiles(projectFiles);
        KotlinCompilerWrapper compilerWrapper = new KotlinCompilerWrapper();
        KotlinCoreEnvironment environment = EnvironmentManager.getEnvironment();
        return compilerWrapper.compile(files, environment.getProject(), environment.getConfiguration(), fileName, searchForMain);
    }

    @Override
    public List<Path> getKotlinRuntimeLibraries() {
        List<Path> libraries = new ArrayList<>();
        libraries.add(jarsFolder.resolve("kotlin-runtime-" + kotlinBuild + ".jar"));
        libraries.add(jarsFolder.resolve("kotlin-reflect-" + kotlinBuild + ".jar"));
        libraries.add(jarsFolder.resolve("kotlin-test-" + kotlinBuild + ".jar"));
        libraries.addAll(getCompileTimeLibraries());
        return libraries;
    }

    @Override
    public Path getKotlinRuntimeJar() {
        return jarsFolder.resolve("kotlin-runtime-" + kotlinBuild + ".jar");
    }

    @Override
    public Path getWrapperFolder() {
        return wrapperFolder;
    }

    @Override
    public String getWrapperVersion() {
        return kotlinVersion;
    }

    @Override
    public MethodPositions getMethodPositions(Map<String, byte[]> classFiles) {
        MethodPositions methodPositions= new MethodPositions();
        for(String key : classFiles.keySet()){
            methodPositions.addClassMethodPositions(key, MethodsFinder.readClassMethodPositions(classFiles.get(key), key));
        }
        return methodPositions;
    }

    private List<KtFile> createPsiFiles(Map<String, String> files) {
        List<KtFile> result = new ArrayList<>();
        for (String fileName : files.keySet()) {
            result.add(JetPsiFactoryUtil.createFile(EnvironmentManager.getEnvironment().getProject(), fileName, files.get(fileName)));
        }
        return result;
    }

    private List<Path> getCompileTimeLibraries (){
        List<Path> libraries = new ArrayList<>();
        libraries.add(jarsFolder.resolve("annotations-13.0.jar"));
        libraries.add(jarsFolder.resolve("kotlinx-coroutines-core-0.14.jar"));
        libraries.add(jarsFolder.resolve("kotlin-stdlib-" + stdlibVersion + ".jar"));
        libraries.add(jarsFolder.resolve("kotlin-stdlib-jre7-" + stdlibVersion + ".jar"));
        libraries.add(jarsFolder.resolve("kotlin-stdlib-jre8-" + stdlibVersion + ".jar"));
        return libraries;
    }
}
