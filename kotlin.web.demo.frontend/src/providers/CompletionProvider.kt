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
import checkDataForException
import checkDataForNull
import generateAjaxUrl
import model.Project
import utils.ajax
import utils.jquery
import kotlin.browser.document

class CompletionProvider() {

    var onSuccess: (dynamic) -> Unit = {

    }
    var onFail: (dynamic) -> Unit = {

    }


    private var isLoadingCompletion = false;

    fun getCompletion(project: Project, filename: String, cursor: dynamic, callback:(dynamic) -> Unit) {
        if (!isLoadingCompletion) {
            isLoadingCompletion = true;
            jquery.ajax(json(
                //runConf is unused parameter. It's added to url for useful access logs
                "url" to generateAjaxUrl("complete", json( "runConf" to project.confType )),
                "context" to document.body,
                "success" to { data: dynamic ->
                    isLoadingCompletion = false;
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            onSuccess(data);
                            callback(data);
                        } else {
                            onFail(data);
                        }
                    } else {
                        onFail("Incorrect data format.");
                    }
                },
                "dataType" to "json",
                "timeout" to 10000,
                "type" to "POST",
                "data" to json(
                    "project" to JSON.stringify(project),
                    "filename" to filename,
                    "line" to cursor.line,
                    "ch" to cursor.ch
                ),
                "error" to  { jqXHR: dynamic, textStatus: String, errorThrown: String ->
                    isLoadingCompletion = false;
                    if (jqXHR.responseText != null && jqXHR.responseText != "") {
                        onFail(jqXHR.responseText);
                    } else {
                        onFail(textStatus + " : " + errorThrown);
                    }
                }
            ));
        }
    }
}