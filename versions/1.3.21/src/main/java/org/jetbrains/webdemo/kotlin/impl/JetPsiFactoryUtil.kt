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

object JetPsiFactoryUtil {

    fun createFile(project: Project, name: String, text: String): KtFile {
        val nameWithExtension = if (name.endsWith(".kt")) name else "$name.kt"
        val virtualFile = LightVirtualFile(nameWithExtension, KotlinLanguage.INSTANCE, text)
        virtualFile.charset = CharsetToolkit.UTF8_CHARSET
        return (PsiFileFactory.getInstance(project) as PsiFileFactoryImpl).trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile
    }

}
