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
package org.jetbrains.webdemo.kotlin.impl.translator

import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.facade.K2JSTranslator
import org.jetbrains.kotlin.js.facade.MainCallParameters
import org.jetbrains.kotlin.js.facade.TranslationResult
import org.jetbrains.kotlin.js.facade.exceptions.TranslationException
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor
import org.jetbrains.webdemo.kotlin.exceptions.KotlinCoreException
import org.jetbrains.webdemo.kotlin.impl.WrapperSettings
import org.jetbrains.webdemo.kotlin.impl.analyzer.ErrorAnalyzer
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager
import java.util.*

object WebDemoTranslatorFacade {

    fun translateProjectWithCallToMain(files: List<KtFile>, arguments: Array<String>): org.jetbrains.webdemo.kotlin.datastructures.TranslationResult {
        try {
            return doTranslate(files, arguments)
        } catch (e: Throwable) {
            throw KotlinCoreException(e)
        } finally {
            EnvironmentManager.reinitializeJavaEnvironment()
        }
    }

    @Throws(TranslationException::class)
    private fun doTranslate(
            files: List<KtFile>,
            arguments: Array<String>
    ): org.jetbrains.webdemo.kotlin.datastructures.TranslationResult {
        val environment = EnvironmentManager.getEnvironment()
        val currentProject = environment.project

        val configuration = environment.configuration.copy()
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "moduleId")
        configuration.put(JSConfigurationKeys.LIBRARIES, listOf(WrapperSettings.JS_LIB_ROOT.toString()))

        val config = JsConfig(currentProject, configuration)
        val reporter = object : JsConfig.Reporter() {
            override fun error(message: String) {}

            override fun warning(message: String) {}
        }
        val translator = K2JSTranslator(config)
        val result = translator.translate(
                reporter,
                files,
                MainCallParameters.mainWithArguments(Arrays.asList(*arguments)))
        return if (result is TranslationResult.Success) {
            org.jetbrains.webdemo.kotlin.datastructures.TranslationResult(
                    "kotlin.kotlin.io.output.flush();\n" + result.getCode() + "\n" + "kotlin.kotlin.io.output.buffer;\n")

        } else {
            val errors = HashMap<String, List<ErrorDescriptor>>()
            for (psiFile in files) {
                errors.put(psiFile.name, ArrayList())
            }
            val errorAnalyzer = ErrorAnalyzer(files, currentProject)
            errorAnalyzer.getErrorsFromDiagnostics(result.diagnostics.all(), errors)
            org.jetbrains.webdemo.kotlin.datastructures.TranslationResult(errors)
        }
    }

}