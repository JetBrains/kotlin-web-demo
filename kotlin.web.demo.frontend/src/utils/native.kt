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

import jquery.JQuery
import org.w3c.dom.HTMLElement
import org.w3c.dom.Window

native("$")
val jquery: JQuery

native
fun Window.eval(code: String): dynamic

native
public fun JQuery.slideDown()

native
public fun JQuery.ajax(parameters: Json)

native
public fun JQuery.height(height: Int)

native
public fun JQuery.children(selector: String): JQuery

native
public fun JQuery.outerHeight(includeMargin: Boolean): Number

native
public fun JQuery.toArray(): Array<HTMLElement>

native
fun unEscapeString(s: String): String

native
fun getFileIdFromUrl(): String

native
val editor: dynamic

native
fun getProjectIdFromUrl(): String?

native
fun isUserProjectInUrl(): Boolean