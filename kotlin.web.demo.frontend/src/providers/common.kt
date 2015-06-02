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

import utils.ajax
import utils.jquery

fun checkDataForNull(data: dynamic): Boolean = data != null

fun checkDataForException(data: dynamic): Boolean {
    return !(data[0] != null && data[0] != undefined && data[0].exception != undefined);
}

public fun ajax(
        url: String,
        success: (dynamic) -> Unit,
        dataType: DataType,
        type: RequestType,
        data: Json? = null,
        timeout: Int,
        error: (dynamic, String, String) -> Unit,
        complete: () -> Unit = {},
        statusCode: Json? = null
){
    jquery.ajax(json(
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

public enum class RequestType() {
    GET,
    POST
}