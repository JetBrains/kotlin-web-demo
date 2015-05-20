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

import JQuery
import checkDataForException
import checkDataForNull
import generateAjaxUrl
import kotlin.browser.document

/**
 * Created by Semyon.Atamas on 5/20/2015.
 */
class ConverterProvider() {
    var beforeConvert = {}
    var onConvertFail: (dynamic) -> Unit = {}
    var onConvertSuccess = {}
    var onConvertComplete = {}

    fun convert(text: String, callback: (String) -> Unit) {
        beforeConvert();
        JQuery.ajax(json(
                "url" to generateAjaxUrl("convertToKotlin", json()),
                "context" to document.body,
                "success" to { data: dynamic ->
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            onConvertSuccess();
                            callback(data[0].text);
                        } else {
                            onConvertFail(data);
                        }
                    } else {
                        onConvertFail("Incorrect data format.");
                    }
                },
                "dataType" to "json",
                "type" to "POST",
                "data" to json("text" to text),
                "timeout" to 10000,
                "error" to  { jqXHR: dynamic, textStatus: String, errorThrown: String ->
                    if (jqXHR.responseText != null && jqXHR.responseText != "") {
                        onConvertFail(jqXHR.responseText);
                    } else {
                        onConvertFail(textStatus + " : " + errorThrown);
                    }
                },
                "complete" to onConvertComplete
        ));
    }
}