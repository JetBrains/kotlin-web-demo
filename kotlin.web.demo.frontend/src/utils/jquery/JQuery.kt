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

package utils.jquery

import jquery.MouseClickEvent
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent


@native("$")
class JQuery {
    companion object JQuery {
        fun ajax(parameters: Json): Promise = noImpl
        fun parseHTML(html: String): Array<HTMLElement>? = noImpl
    }

    class Promise {
        fun then(callback: Function<Unit>): Nothing = noImpl
        fun done(callback: Function<Unit>): Nothing = noImpl
        fun fail(): Nothing = noImpl
        fun always(): Nothing = noImpl
        fun pipe(): Nothing = noImpl
        fun progress(): Nothing = noImpl
        fun state(): Nothing = noImpl
        fun promise(): Nothing = noImpl
    }

    fun html(s: String): Nothing = noImpl
    fun removeClass(className: String): Nothing = noImpl
    fun addClass(className: String): Nothing = noImpl
    fun height(): Number = noImpl
    fun height(value: Int): Nothing = noImpl
    fun width(): Number = noImpl
    fun width(value: Int): Nothing = noImpl
    fun hasClass(className: String): Boolean = noImpl
    fun text(text: String): Nothing = noImpl
    fun click(): Nothing = noImpl
    fun click(handler: Element.(MouseClickEvent) -> Unit): utils.jquery.JQuery = noImpl
    fun slideUp(): Nothing = noImpl
    fun parent(): utils.jquery.JQuery = noImpl
    fun attr(name: String, value: String): Nothing = noImpl
    fun append(any: String): Nothing = noImpl
}

@native
public fun JQuery.slideDown(): Nothing = noImpl

@native
public fun JQuery.height(height: Int): Nothing = noImpl

@native
public fun JQuery.width(width: Int): Nothing = noImpl

@native
public fun JQuery.children(selector: String): JQuery = noImpl

@native
public fun JQuery.outerHeight(includeMargin: Boolean = false): Int = noImpl

@native
public fun JQuery.outerWidth(includeMargin: Boolean = false): Int = noImpl

@native
public fun JQuery.toArray(): Array<HTMLElement> = noImpl

@native("is")
public fun JQuery.isCheck(s: String): Boolean = noImpl

@native
fun JQuery.hide(): Nothing = noImpl

@native
fun JQuery.show(): Nothing = noImpl

@native
fun JQuery.children(): JQuery = noImpl

@native
fun JQuery.unbind(s: String): Nothing = noImpl

@native
fun JQuery.on(s: String, onClose: (Event) -> Unit): Nothing = noImpl

@native
fun JQuery.css(key: String, value: dynamic): Nothing = noImpl

@native("val")
fun JQuery.value(): String = noImpl

@native("val")
fun JQuery.value(s: String): Nothing = noImpl

@native("$")
fun jq(document: Document): JQuery = noImpl

@native("$")
fun jq(selector: String): JQuery = noImpl

@native("$")
fun jq(element: Element): JQuery = noImpl

@native("$")
fun jq(jq: JQuery): JQuery = noImpl

@native
fun JQuery.circleProgress(options: Json): Nothing = noImpl

@native
fun JQuery.on(action: String, selector: String, callback: (event: Event) -> Unit): Nothing = noImpl

@native
fun JQuery.keydown(callback: (KeyboardEvent) -> Unit): Nothing = noImpl

@native
fun JQuery.find(selector: String): JQuery = noImpl

@native
fun JQuery.trigger(action: String): Nothing = noImpl

@native
fun JQuery.focus(): Nothing = noImpl

@native
operator fun JQuery.get(index: Int): Element = noImpl