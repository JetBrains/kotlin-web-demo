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
import utils.DataType
import utils.RequestType
import utils.ajax
import kotlin.browser.document

/**
 * Created by Semyon.Atamas on 5/20/2015.
 */


class ConverterProvider(){
    fun convert(text: String, onSuccess: (String) -> Unit, onFail: (dynamic) -> Unit, onComplete: () -> Unit) {
        ajax(
                url = generateAjaxUrl("convertToKotlin", json()),
                success = { data: dynamic ->
                    if (checkDataForNull(data)) {
                        if (checkDataForException(data)) {
                            onSuccess(data[0].text);
                        } else {
                            onFail(data);
                        }
                    } else {
                        onFail("Incorrect data format.");
                    }
                },
                dataType = DataType.JSON,
                type = RequestType.POST,
                data = json("text" to text),
                timeout = 10000,
                error = { jqXHR: dynamic, textStatus: String, errorThrown: String ->
                    if (jqXHR.responseText != null && jqXHR.responseText != "") {
                        onFail(jqXHR.responseText);
                    } else {
                        onFail(textStatus + " : " + errorThrown);
                    }
                },
                complete = onComplete
        );
    }
}