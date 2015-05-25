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

import checkDataForNull
import generateAjaxUrl
import jquery.jq
import utils.*
import kotlin.browser.document
import kotlin.browser.window

/**
 * Created by Semyon.Atamas on 5/25/2015.
 */

class LoginProvider(
        private val beforeLogout: () -> Unit,
        private val onLogout: (dynamic) -> Unit,
        private val onLogin: (dynamic) -> Unit,
        private val onFail: (String, String) -> Unit
) {

    fun login(type: String) {
        window.location.assign(window.location.protocol + "//" + window.location.host + "/login/" + type)
    }

    fun logout() {
        blockContent();
        beforeLogout();
        ajax(
                url = window.location.protocol + "//" + window.location.host + "/logout",
                success = { data ->
                    try {
                        onLogout(data);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.TEXT,
                type = RequestType.GET,
                timeout = 10000,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.login_fail);
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun getUserName() {
        blockContent();
        ajax(
                url = generateAjaxUrl("getUserName", json()),
                success = { data ->
                    try {
                        if (checkDataForNull(data)) {
                            onLogin(data);
                        } else {
                            onFail("Username is null.", ActionStatusMessages.login_fail);
                        }
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                dataType = DataType.JSON,
                type = RequestType.GET,
                timeout = 10000,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.login_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        );
    }
}