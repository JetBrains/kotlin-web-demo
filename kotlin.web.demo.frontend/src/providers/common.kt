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
import org.w3c.fetch.Request
import utils.Object
import utils.jquery.JQuery
import views.editor.Diagnostic

fun checkDataForNull(data: dynamic): Boolean = data != null

fun checkDataForException(data: dynamic): Boolean {
    return !(data[0] != null && data[0] != undefined && data[0].exception != undefined)
}

//TODO add defaults and move generate urls into fun
public fun ajax(
        url: String,
        success: (dynamic) -> Unit = {},
        dataType: DataType,
        type: HTTPRequestType,
        data: Json? = null,
        timeout: Int = 10000,
        error: (dynamic, String, String) -> Unit = {jqXHR, textStatus, errorThrown ->},
        complete: () -> Unit = {},
        statusCode: Json? = null
){
    JQuery.ajax(json(
            "url" to url,
            "success" to success,
            "dataType" to dataType,
            "type" to type.name().toLowerCase(),
            "data" to (data ?: undefined),
            "timeout" to timeout,
            "error" to error,
            "complete" to complete,
            "statusCode" to (statusCode ?: undefined)
    ))
}

public enum class DataType() {
    TEXT,
    JSON
}

public enum class HTTPRequestType() {
    GET,
    POST
}

fun generateAjaxUrl(type: String, parameters: Map<String, String> = emptyMap()): String {
    var url = "kotlinServer?sessionId=" + sessionId + "&type=" + type
    for (entry in parameters) {
        url += "&" + entry.getKey() + "=" + entry.getValue()
    }
    return url
}

fun generateAjaxUrl(type: REQUEST_TYPE, parameters: Map<String, String> = emptyMap()): String{
    return generateAjaxUrl(type.value, parameters)
}

fun getErrorsMapFromObject(obj: dynamic, project: Project): Map<File, Array<Diagnostic>> {
    val errors = hashMapOf<File, Array<Diagnostic>>()
    for (fileName in Object.keys(obj)) {
        val file = project.files.firstOrNull { it.name == fileName }
        //file can be null if it's hidden.
        if (file != null) errors.put(file, obj[fileName])
    }
    return errors
}

var sessionId = "-1"