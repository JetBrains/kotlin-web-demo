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

package org.jetbrains.webdemo.backend

object BackendOutputMessages {

    private const val ERROR_TAG_START = "<errStream>"
    private const val OUTPUT_TAG_START = "<outStream>"
    private const val OUTPUT_TAG_END = "</outStream>"
    private const val ERROR_TAG_END = "</errStream>"

    private const val KOTLIN_COMPILER_ERROR_MESSAGE = "${ERROR_TAG_START}BUG$ERROR_TAG_END"
    const val KOTLIN_TIMEOUT_MESSAGE = "${ERROR_TAG_START}Program was terminated after 10s.⌛️$ERROR_TAG_END"
    const val KOTLIN_LONG_OUTPUT_MESSAGE = "${ERROR_TAG_START}Your program produces too much output!$ERROR_TAG_END"

    fun buildKotlinCompilerErrorMessage(stackTrace: String): String {
        return "$KOTLIN_COMPILER_ERROR_MESSAGE$OUTPUT_TAG_START\nMessage:\n$stackTrace$OUTPUT_TAG_END"
    }
}