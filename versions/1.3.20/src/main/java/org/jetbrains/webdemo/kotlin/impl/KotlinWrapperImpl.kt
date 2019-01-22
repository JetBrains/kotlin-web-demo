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

@file:Suppress("PRE_RELEASE_CLASS")

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
    private lateinit var jarsFolder: Path
    private var kotlinVersion: String? = null
    private var kotlinBuild: String? = null
    private lateinit var wrapperFolder: Path
    private lateinit var userLibFolder: Path

    override fun init(javaLibraries: List<Path>, config: KotlinVersionConfig) {
        this.kotlinVersion = config.version
        this.kotlinBuild = config.build
        WrapperLogger.init(kotlinVersion!!)
        wrapperFolder = KotlinWrappersManager.wrappersDir.resolve(kotlinVersion)
        jarsFolder = wrapperFolder.resolve("kotlin")
        userLibFolder = wrapperFolder.resolve("libraries")
        WrapperSettings.JS_LIB_ROOT = wrapperFolder.resolve("js")
        val libraries = kotlinLibraries
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
//        runBlocking {
//            repeat(100) { // launch a lot of coroutines
//                launch {
//                    try {
//                        WebDemoTranslatorFacade.translateProjectWithCallToMain(ktFiles, args)
//                    } catch (e: Exception) {
//                        println(e)
//                    }
//                }
//            }
//        }
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

    /**
     * Get user jar-libraries from 'libraries' folder
     * For adding user-library please add dependencies to build.gradle
     * Use 'library' task for downloading dependency
     *
     * @return list of [Path] to library
     */
    override fun getKotlinLibraries(): MutableList<Path> {
        val libraries = ArrayList<Path>()
        userLibFolder.toFile().listFiles { pathname -> pathname.absolutePath.matches(Regex(".*\\.jar$"))}
                ?.forEach { libraries.add(it.toPath()) }
        return libraries
    }

    override fun getKotlinRuntimeJar(): Path {
        return userLibFolder.resolve("kotlin-stdlib-$kotlinBuild.jar")
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
