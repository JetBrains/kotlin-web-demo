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

import kotlin.js.json

class ConverterProvider {
    fun convert(text: String, onSuccess: (String) -> Unit, onFail: (dynamic) -> Unit, onComplete: () -> Unit) {
        ajax(
                url = generateAjaxUrl("convertToKotlin"),
                success = onSuccess,
                dataType = DataType.TEXT,
                type = HTTPRequestType.POST,
                data = json("text" to text),
                timeout = 10000,
                error = { jqXHR: dynamic, textStatus: String, errorThrown: String ->
                    if (jqXHR.responseText != null && jqXHR.responseText != "") {
                        onFail(jqXHR.responseText)
                    } else {
                        onFail(textStatus + " : " + errorThrown)
                    }
                },
                complete = onComplete
        )
    }
}