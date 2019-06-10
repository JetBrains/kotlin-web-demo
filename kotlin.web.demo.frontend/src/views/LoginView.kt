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

package views

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import providers.LoginProvider
import utils.decodeURIComponent
import utils.jquery.jq
import kotlin.browser.document
import kotlin.js.json

class LoginView(val loginModel: LoginProvider) {
    var isLoggedIn = false

    fun setUserName(userName: String, type: String) {
        if (userName != "") {
            jq("#login").hide()
            jq("#logout").show()

            isLoggedIn = true
            var decodedUserName = decodeURIComponent(userName)
            decodedUserName = decodedUserName.replace("+", " ")

            jq("#username").text(decodedUserName)
            jq("#logout").find(".icon").addClass(type)
        }
    }

    fun logout() {
        isLoggedIn = false
        jq("#login").show()
        jq("#logout").hide()
    }

    fun login(param: String) {
        loginModel.login(param)
    }

    fun openLoginDialog(onClose: ((Event) -> Unit)? = null) {
        if (onClose != null) {
            jq(loginDialog).on("dialogclose", onClose)
        } else {
            jq(loginDialog).unbind("dialogclose")
        }
        jq(loginDialog).dialog("open")
    }

    var loginDialog = jq("#login-dialog").dialog(json(
            "modal" to true,
            "draggable" to false,
            "resizable" to false,
            "width" to 350,
            "autoOpen" to false,
            "dialogClass" to "login-dialog"
    ))

    init {

        jq(".login-icons").children().click({ e ->
            login(e.target.getAttribute("login-type") as String)
        })

        (document.getElementById("logout-button") as HTMLDivElement).onclick = { event ->
            loginModel.logout()
            event.stopPropagation()
        }
        loginModel.getUserName()
    }

}
