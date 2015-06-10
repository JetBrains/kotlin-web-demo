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
import model.Project
import utils.*
import views.Configuration
import views.ConfigurationType
import views.ConfigurationTypeRunner

class RunProvider(
        private val onSuccess: (List<dynamic>, Project) -> Unit,
        private val onErrorsFound: (dynamic, Project) -> Unit,
        private val onComplete: () -> Unit,
        private val onFail: (String) -> Unit
) {
    fun run(configuration: Configuration, project: Project) {
        if (configuration.type.runner == ConfigurationTypeRunner.JAVA) {
            runJava(project)
        } else {
            loadJsFromServer(project)
        }
    }

    fun checkDataForErrors(data: Array<dynamic>): Boolean {
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

    private fun runJava(project: Project) {
        ajax(
                //runConf is unused parameter. It's added to url for useful access logs
                url = generateAjaxUrl("run", hashMapOf("runConf" to project.confType)),
                success = { data: Array<dynamic> ->
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForErrors(data)) {
                                onSuccess(data.toArrayList(), project)
                            } else {
                                onErrorsFound(data, project)
                            }
                        } else {
                            onFail("Incorrect data format.")
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.JSON,
                type = HTTPRequestType.POST,
                data = json ("project" to JSON.stringify(project)),
                timeout = 10000,
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
                success = { data: Array<dynamic> ->
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForErrors(data)) {
                                var output = arrayListOf<dynamic>()
                                for (element in data) {
                                    if (element.type == "generatedJSCode") {
                                        try {
                                            //Placed here because of firefox bug
                                            //(error modifying context of canvas in invisible iframe)
                                            if (runConfiguration ==
                                                    ConfigurationType.CANVAS.name().toLowerCase()) {
                                                Application.iframeDialog.open()
                                            }
                                            var out: String = Application.iframe.contentWindow!!.eval(element.text)
                                            output.add(json("text" to unEscapeString(out), "type" to "jsOut"))
                                        } catch (e: Throwable) {
                                            output.add(json("type" to "jsException", "exception" to e))
                                        } finally {
                                            if (runConfiguration == "js") {
                                                Application.iframe.clear()
                                            }
                                        }
                                    }
                                    output.add(element)
                                }
                                onSuccess(output, project)
                            } else {
                                onErrorsFound(data, project)
                            }
                        } else {
                            onFail("Incorrect data format.")
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
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