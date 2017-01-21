/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package providers

import application.Application
import model.File
import model.Project
import utils.Object
import utils.eval
import views.Configuration
import views.ConfigurationType
import views.ConfigurationTypeRunner
import views.editor.Diagnostic
import java.util.*
import kotlin.js.native

class RunProvider(
        private val beforeRun: () -> Unit,
        private val onSuccess: (RunResult, Project) -> Unit,
        private val processTranslateToJSResult: (TranslationResult, Project) -> Unit,
        private val onComplete: () -> Unit,
        private val onFail: (String) -> Unit
) {
    fun run(configuration: Configuration, project: Project, file: File) {
        if (project.files.isEmpty()) return;
        beforeRun()
        if (configuration.type.runner == ConfigurationTypeRunner.JAVA) {
            runJava(project, file)
        } else {
            loadJsFromServer(project)
        }
    }

    private fun checkDataForErrors(data: Array<dynamic>): Boolean {
        val hasErrors = data.any { element ->
            if (element.type == "errors") {
                var containsErrors = false
                for (fileName in Object.keys(element.errors)) {
                    var fileErrorsAndWarnings: Array<dynamic> = element.errors[fileName]
                    containsErrors = fileErrorsAndWarnings.any({
                        it.severity == "ERROR"
                    })
                }
                containsErrors
            } else {
                false
            }
        }
        return !hasErrors
    }

    private fun runJava(project: Project, file: File) {
        ajax(
                //runConf is unused parameter. It's added to url for useful access logs
                url = generateAjaxUrl("run", hashMapOf("runConf" to project.confType)),
                success = { data: dynamic ->
                    val errors = getErrorsMapFromObject(data.errors, project)
                    if (project.confType == "junit") {
                        onSuccess(JunitExecutionResult(errors, data.testResults), project)
                    } else {
                        onSuccess(JavaRunResult(errors, data.text, data.exception), project);
                    }
                },
                dataType = DataType.JSON,
                type = HTTPRequestType.POST,
                data = json ("project" to JSON.stringify(project), "filename" to file.name),
                timeout = 15000,
                complete = { onComplete() },
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        if (jqXHR.responseText != null && jqXHR.responseText != "") {
                            onFail(jqXHR.responseText)
                        } else {
                            onFail(textStatus + " : " + errorThrown)
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                }
        )

    }

    fun loadJsFromServer(project: Project) {
        var runConfiguration = project.confType
        ajax(
                //runConf is unused parameter. It's added to url for useful access logs
                url = generateAjaxUrl("run", hashMapOf("runConf" to runConfiguration)),
                success = { data: dynamic ->
                    var translationResult: TranslationResult;
                    val errors = getErrorsMapFromObject(data.errors, project)
                    if (data.jsCode != null) {
                        val kotlinVersion = project.compilerVersion ?: Application.versionView.defaultVersion
                        val iframeDialog = Application.getIframeDialog(kotlinVersion)

                        try {
                            //Placed here because of firefox bug
                            //(error modifying context of canvas in invisible iframe)
                            if (runConfiguration ==
                                    ConfigurationType.CANVAS.name.toLowerCase()) {
                                iframeDialog.open()
                            }
                            val out: String = iframeDialog.iframe.contentWindow!!.eval(data.jsCode)
                            translationResult = TranslationResult(errors, data.jsCode, out, null)
                        } catch (e: Throwable) {
                            translationResult = TranslationResult(errors, data.jsCode, null, e)
                        } finally {
                            if (runConfiguration == "js") {
                                iframeDialog.iframe.contentWindow!!.location.reload()
                            }
                        }
                    } else {
                        translationResult = TranslationResult(errors, null, null, null)
                    }
                    processTranslateToJSResult(translationResult, project)
                },
                dataType = DataType.JSON,
                type = HTTPRequestType.POST,
                data = json("project" to JSON.stringify(project)),
                timeout = 10000,
                complete = {
                    onComplete()
                },
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        if (jqXHR.responseText != null && jqXHR.responseText != "") {
                            onFail(jqXHR.responseText)
                        } else {
                            onFail(textStatus + " : " + errorThrown)
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                }
        )
    }
}

open class RunResult(
        val errors: Map<File, List<Diagnostic>>
)

class JavaRunResult(
        errors: Map<File, List<Diagnostic>>,
        val text: String?,
        val exception: ExceptionDescriptor?
) : RunResult(errors)

@native interface ExceptionDescriptor {
    val message: String
    val fullName: String
    val stackTrace: List<StackTraceElement>
    val cause: ExceptionDescriptor
}

@native interface StackTraceElement {
    val declaringClass: String
    val methodName: String
    val fileName: String
    val lineNumber: Int
}

class JunitExecutionResult(
        errors: Map<File, List<Diagnostic>>,
        testResults: dynamic
) : RunResult(errors) {
    val testResults = HashMap<String, List<TestResult>>()

    init {
        for (className in Object.keys(testResults)) {
            val classTests: Array<TestResult> = testResults[className]
            this.testResults.put(className, classTests.asList())
        }
    }
}

@native
interface TestResult {
    val output: String
    val sourceFileName: String
    val className: String
    val methodName: String
    val executionTime: Int
    val exception: ExceptionDescriptor?
    val comparisonFailure: ComparisonFailureDescriptor?
    val methodPosition: Int
    val status: String
}

@native interface ComparisonFailureDescriptor : ExceptionDescriptor {
    val expected: String
    val actual: String
}

enum class Status {
    OK,
    FAIL,
    ERROR
}

class TranslationResult(
        errors: Map<File, List<Diagnostic>>,
        val jsCode: String?,
        val output: String?,
        val exception: Throwable?
) : RunResult(errors);

