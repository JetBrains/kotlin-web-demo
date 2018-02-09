/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.impl

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.webdemo.KotlinVersionConfig
import org.jetbrains.webdemo.kotlin.KotlinWrapper
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager
import org.jetbrains.webdemo.kotlin.datastructures.*
import org.jetbrains.webdemo.kotlin.impl.analyzer.ErrorAnalyzer
import org.jetbrains.webdemo.kotlin.impl.compiler.KotlinCompilerWrapper
import org.jetbrains.webdemo.kotlin.impl.completion.CompletionProvider
import org.jetbrains.webdemo.kotlin.impl.converter.WebDemoJavaToKotlinConverter
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager
import org.jetbrains.webdemo.kotlin.impl.translator.WebDemoTranslatorFacade
import java.nio.file.Path
import java.util.*

class KotlinWrapperImpl : KotlinWrapper {
    private var jarsFolder: Path? = null
    private var kotlinVersion: String? = null
    private var kotlinBuild: String? = null
    private var wrapperFolder: Path? = null
    private var stdlibVersion: String? = null

    private val compileTimeLibraries: List<Path>
        get() {
            val libraries = ArrayList<Path>()
            libraries.add(jarsFolder!!.resolve("annotations-13.0.jar"))
            libraries.add(jarsFolder!!.resolve("kotlinx-coroutines-core-0.17.jar"))
            libraries.add(jarsFolder!!.resolve("kotlin-stdlib-$stdlibVersion.jar"))
            libraries.add(jarsFolder!!.resolve("kotlin-stdlib-jre7-$stdlibVersion.jar"))
            libraries.add(jarsFolder!!.resolve("kotlin-stdlib-jre8-$stdlibVersion.jar"))
            return libraries
        }

    override fun init(javaLibraries: List<Path>, config: KotlinVersionConfig) {
        this.kotlinVersion = config.version
        this.kotlinBuild = config.build
        stdlibVersion = config.stdlibVersion
        WrapperLogger.init(kotlinVersion!!)
        wrapperFolder = KotlinWrappersManager.wrappersDir.resolve(kotlinVersion)
        jarsFolder = wrapperFolder!!.resolve("kotlin")
        WrapperSettings.JS_LIB_ROOT = wrapperFolder!!.resolve("js")
        val libraries = kotlinRuntimeLibraries
        libraries.addAll(javaLibraries)
        EnvironmentManager.init(libraries)
    }

    override fun translateJavaToKotlin(javaCode: String): String {
        return WebDemoJavaToKotlinConverter.getResult(javaCode)
    }

    override fun getErrors(files: Map<String, String>, isJs: Boolean): Map<String, List<ErrorDescriptor>> {
        val psiFiles = createPsiFiles(files)
        val analyzer = ErrorAnalyzer(psiFiles, EnvironmentManager.getEnvironment().project)
        return analyzer.getAllErrors(isJs)
    }

    override fun compileKotlinToJS(files: Map<String, String>, args: Array<String>): TranslationResult {
        val ktFiles = createPsiFiles(files)
        return WebDemoTranslatorFacade.translateProjectWithCallToMain(ktFiles, args)
    }

    override fun getCompletionVariants(
            projectFiles: Map<String, String>, filename: String, line: Int, ch: Int, isJs: Boolean): List<CompletionVariant> {
        val files = createPsiFiles(projectFiles)
        val completionProvider = CompletionProvider(files, filename, line, ch)
        return completionProvider.getResult(isJs)
    }

    override fun compileCorrectFiles(projectFiles: Map<String, String>, fileName: String?, searchForMain: Boolean): CompilationResult {
        val files = createPsiFiles(projectFiles)
        val compilerWrapper = KotlinCompilerWrapper()
        val environment = EnvironmentManager.getEnvironment()
        return compilerWrapper.compile(files, environment.project, environment.configuration, fileName!!, searchForMain)
    }

    override fun getKotlinRuntimeLibraries(): MutableList<Path> {
        val libraries = ArrayList<Path>()
        libraries.add(jarsFolder!!.resolve("kotlin-runtime-$kotlinBuild.jar"))
        libraries.add(jarsFolder!!.resolve("kotlin-reflect-$kotlinBuild.jar"))
        libraries.add(jarsFolder!!.resolve("kotlin-test-$kotlinBuild.jar"))
        libraries.addAll(compileTimeLibraries)
        return libraries
    }

    override fun getKotlinRuntimeJar(): Path {
        return jarsFolder!!.resolve("kotlin-runtime-$kotlinBuild.jar")
    }

    override fun getWrapperFolder(): Path? {
        return wrapperFolder
    }

    override fun getWrapperVersion(): String? {
        return kotlinVersion
    }

    override fun getMethodPositions(classFiles: Map<String, ByteArray>): MethodPositions {
        val methodPositions = MethodPositions()
        for (key in classFiles.keys) {
            methodPositions.addClassMethodPositions(key, MethodsFinder.readClassMethodPositions(classFiles[key]!!, key))
        }
        return methodPositions
    }

    private fun createPsiFiles(files: Map<String, String>): MutableList<KtFile> {
        return files.keys.mapTo(ArrayList()) { JetPsiFactoryUtil.createFile(EnvironmentManager.getEnvironment().project, it, files[it]!!) }
    }
}
