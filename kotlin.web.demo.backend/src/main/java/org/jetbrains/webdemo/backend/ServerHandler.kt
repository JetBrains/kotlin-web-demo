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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.webdemo.ErrorWriter
import org.jetbrains.webdemo.Project
import org.jetbrains.webdemo.backend.executor.ExecutorUtils
import org.jetbrains.webdemo.backend.executor.result.JavaExecutionResult
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ServerHandler {

    private val objectMapper = jacksonObjectMapper()

    @Throws(IOException::class)
    fun handle(request: HttpServletRequest, response: HttpServletResponse) {

        if (request.queryString != null && request.queryString == "test") {
            try {
                val wrapper = KotlinWrappersManager.defaultWrapper
                val projectString = "{\"compilerVersion\":null,\"id\":\"/Examples/Hello,%20world!/Simplest%20version\",\"originUrl\":\"/Examples/Hello,%20world!/Simplest%20version\",\"name\":\"Simplest version\",\"args\":\"\",\"confType\":\"java\",\"files\":[{\"name\":\"Simplest version.kt\",\"text\":\"fun main(args: Array<String>) {\\n    println(\\\"Hello, world!\\\")\\n}\",\"publicId\":\"/Examples/Hello,%20world!/Simplest%20version/Simplest%20version.kt\",\"modifiable\":true,\"type\":\"KOTLIN_FILE\"}],\"readOnlyFileNames\":[],\"expectedOutput\":null}"
                val project = objectMapper.readValue<Project>(projectString)
                val files = mutableMapOf<String, String>()
                project.files.forEach { files[it.name] = it.text }

                val errors = wrapper.getErrors(files, false)
                val filename = "Simplest version.kt"
                val searchForMain = true
                val compilationResult = wrapper.compileCorrectFiles(files, filename, searchForMain)
                val executionResult = ExecutorUtils.executeCompiledFiles(
                        compilationResult.files,
                        compilationResult.mainClass,
                        wrapper.kotlinLibraries,
                        project.args,
                        wrapper.wrapperFolder.resolve("executors.policy"),
                        false)

                val isServerWork = (executionResult as JavaExecutionResult).text.contains("Hello, world")

                if (isServerWork) {
                    response.sendResponse(HttpServletResponse.SC_OK, "All is fine")
                } else {
                    response.sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Bad result!")
                }
            } catch (e: Exception) {
                response.sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                ErrorWriter.log.error("Health check doesn't work", e)
            }

        } else {
            try {
                val session = MyHttpSession()
                session.handle(request, response)
            } catch (e: Throwable) {
                //Do not stop server
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        "UNKNOWN", "unknown", request.requestURI + "?" + request.queryString)
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            }

        }
    }

}

