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

package utils

import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.Location
import views.dialogs.Dialog
import kotlin.browser.document

enum class KeyCode (val code: Int){
    S(83),
    R(82),
    F9(120),
    ENTER(13),
    ESCAPE(27)
}

native
val Location.protocol: String

native
val Location.host: String

native
fun blockContent()

native
fun unBlockContent()

native
val ActionStatusMessages: dynamic = noImpl

native
val Configuration: dynamic

native
val ConfigurationType: dynamic

native
val Object: dynamic

fun HTMLIFrameElement.clear(){
    this.contentWindow!!.location.reload();
}

native
fun safe_tags_replace(string: String): String