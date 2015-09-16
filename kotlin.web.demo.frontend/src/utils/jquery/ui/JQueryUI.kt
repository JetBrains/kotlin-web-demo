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

package utils.jquery.ui

import utils.jquery.JQuery

@native
fun JQuery.toggle()

@native
fun JQuery.resizable(parameters: Json)

@native
fun JQuery.resizable(mode : String, param : String, value : Any?)

@native
fun JQuery.selectmenu(parameters: Json)

@native
fun JQuery.selectmenu(command: String)

@native
fun JQuery.tabs()

@native
public fun JQuery.button(command : String) : JQuery = noImpl

@native
public fun JQuery.button(parameters : Json) : JQuery = noImpl

@native
public fun JQuery.dialog(command : String) : JQuery = noImpl

@native
public fun JQuery.dialog(parameters : Json) : JQuery = noImpl

@native
fun JQuery.dialog(mode : String, param : String, value : Any?)

@native
fun JQuery.accordion(params: Json): JQuery

@native
fun JQuery.accordion(command: String): JQuery

@native
fun JQuery.accordion(command: String, name: String, value: Any): JQuery

@native
public fun JQuery.button(mode : String, param : String, value : Any?) : JQuery = noImpl

@native
public fun JQuery.tabs(mode : String, param : String, value : Any?) : JQuery = noImpl