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
package org.jetbrains.webdemo.kotlin.impl.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult
import org.jetbrains.webdemo.kotlin.exceptions.KotlinCompileException
import org.jetbrains.webdemo.kotlin.impl.ResolveUtils
import org.jetbrains.webdemo.kotlin.impl.WrapperLogger
import java.util.*

class KotlinCompilerWrapper {
    fun compile(currentPsiFiles: List<KtFile>, currentProject: Project, configuration: CompilerConfiguration, fileName: String, searchForMain: Boolean): CompilationResult {
        try {
            val generationState = ResolveUtils.getGenerationState(currentPsiFiles, currentProject, configuration)
            val mainClass = findMainClass(generationState.bindingContext, currentPsiFiles, fileName, searchForMain)
            KotlinCodegenFacade.compileCorrectFiles(generationState) { throwable, fileUrl -> WrapperLogger.reportException("Compilation error at file " + fileUrl, throwable) }

            val factory = generationState.factory
            val files = HashMap<String, ByteArray>()
            for (file in factory.asList()) {
                files.put(file.relativePath, file.asByteArray())
            }
            return CompilationResult(files, mainClass)
        } catch (e: Throwable) {
            throw KotlinCompileException(e)
        }

    }

    private fun findMainClass(bindingContext: BindingContext, files: List<KtFile>, fileName: String, searchForMain: Boolean): String {
        val mainFunctionDetector = MainFunctionDetector(bindingContext)
        files
                .filter { it.name.contains(fileName) && (!searchForMain || mainFunctionDetector.hasMain(it.declarations)) }
                .forEach { return getMainClassName(it) }
        for (file in files) {
            if (mainFunctionDetector.hasMain(file.declarations)) {
                return getMainClassName(file)
            }
        }
        return getMainClassName(files.iterator().next())
    }

    private fun getMainClassName(file: KtFile): String {
        return PackagePartClassUtils.getPackagePartFqName(file.packageFqName, file.name).asString()
    }
}
