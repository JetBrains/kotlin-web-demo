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

import model.File
import model.Project
import views.editor.Error
import utils.Object

class HighlightingProvider(
        private val onSuccess: (dynamic) -> Unit,
        private val onFail: (String, String) -> Unit
) {

    fun getHighlighting(project: Project, callback: (Map<File, Array<Error>>) -> Unit, finallyCallback: (() -> Unit)?) {
        ajax(
                //runConf is unused parameter. It's added to url for useful access logs
                url = generateAjaxUrl("highlight", hashMapOf("runConf" to project.confType)),
                success = { data ->
                    try {
                        val errors = getErrorsMapFromObject(data, project)
                        onSuccess(errors);
                        callback(errors);
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                dataType = DataType.JSON,
        type = HTMLRequestType.POST,
        data = json( "project" to JSON.stringify(project) ),
        timeout = 10000,
        error = { jqXHR, textStatus, errorThrown ->
            try {
                if (jqXHR.responseText != null && jqXHR.responseText != "") {
                    onFail(jqXHR.responseText, "");
                } else {
                    onFail(textStatus + " : " + errorThrown, "");
                }
            } catch (e: Throwable) {
                console.log(e)
            }
        },
        complete = {
            finallyCallback?.invoke()
        }
        )
    }
}