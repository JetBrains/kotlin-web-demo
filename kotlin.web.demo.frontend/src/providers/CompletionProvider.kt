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
import model.Project
import utils.codemirror.Position
import views.ActionStatusMessage

class CompletionProvider(
        var onSuccess: () -> Unit,
        var onFail: (error: String, status: ActionStatusMessage) -> Unit
)
{
    private var isLoadingCompletion = false

    fun getCompletion(
            project: Project,
            filename: String,
            cursor: Position,
            callback: (Array<CompletionProposal>) -> Unit
    ) {
        if (isLoadingCompletion) return

        isLoadingCompletion = true
        ajax(url = generateAjaxUrl(REQUEST_TYPE.COMPLETE, hashMapOf("runConf" to project.confType)),
                dataType = DataType.JSON,
                timeout = 10000,
                type = HTMLRequestType.POST,
                data = json(
                        "project" to JSON.stringify(project),
                        "filename" to filename,
                        "line" to cursor.line,
                        "ch" to cursor.ch
                ),
                success = { completionProposals: Array<CompletionProposal> ->
                    isLoadingCompletion = false
                    onSuccess()
                    callback(completionProposals)
                },
                error =  { jqXHR: dynamic, textStatus: String, errorThrown: String ->
                    isLoadingCompletion = false
                    if (jqXHR.responseText != null && jqXHR.responseText != "") {
                        onFail(jqXHR.responseText, ActionStatusMessage.get_completion_fail)
                    } else {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.get_completion_fail)
                    }
                }
        )
    }
}


public data class CompletionProposal(val icon: String, val text: String, val displayText: String, val tail: String)

