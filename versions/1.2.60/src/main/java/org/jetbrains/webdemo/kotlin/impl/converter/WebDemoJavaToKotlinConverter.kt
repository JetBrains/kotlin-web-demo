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

package org.jetbrains.webdemo.kotlin.impl.converter

import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import org.jetbrains.kotlin.j2k.ConverterSettings
import org.jetbrains.kotlin.j2k.EmptyJavaToKotlinServices
import org.jetbrains.kotlin.j2k.JavaToKotlinConverter
import org.jetbrains.kotlin.j2k.JavaToKotlinTranslator
import org.jetbrains.webdemo.kotlin.impl.ResolveUtils
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager
import java.lang.reflect.InvocationTargetException
import java.util.*

object WebDemoJavaToKotlinConverter {

    @Synchronized
    fun getResult(code: String): String {
        val project = EnvironmentManager.getEnvironment().project
        val converter = JavaToKotlinConverter(
                project,
                ConverterSettings.defaultSettings,
                EmptyJavaToKotlinServices)
        val instance = PsiElementFactory.SERVICE.getInstance(project)

        val javaFile = PsiFileFactory.getInstance(project).createFileFromText("test.java", JavaLanguage.INSTANCE, code)

        //To create a module
        ResolveUtils.getBindingContext(arrayListOf(), project, false)

        var inputElements: List<PsiElement>? = if (javaFile.children.any { it is PsiClass }) listOf<PsiElement>(javaFile) else null

        if (inputElements == null) {
            val psiClass = instance.createClassFromText(code, javaFile)
            var errorsFound = false
            for (element in psiClass.children) {
                if (element is PsiErrorElement) {
                    errorsFound = true
                }
            }
            if (!errorsFound) {
                inputElements = Arrays.asList(*psiClass.children)
            }
        }

        if (inputElements == null) {
            val codeBlock = instance.createCodeBlockFromText("{$code}", javaFile)
            val childrenWithoutBraces = Arrays.copyOfRange(codeBlock.children, 1, codeBlock.children.size - 1)
            inputElements = Arrays.asList(*childrenWithoutBraces)
        }

        val resultFormConverter = converter.elementsToKotlin(inputElements!!).results
        var textResult = ""
        resultFormConverter
                .asSequence()
                .filterNotNull()
                .forEach { it -> textResult = textResult + it.text + "\n" }
        textResult = prettify(textResult)
        return textResult
    }

    private fun prettify(code: String): String {
        try {
            val method = JavaToKotlinTranslator.javaClass.getDeclaredMethod("prettify", String::class.java)
            method.isAccessible = true
            return method.invoke(JavaToKotlinTranslator, code) as String
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }

    }

}

