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
import kotlin.js.Json


@JsName("$")
external class JQuery {
    companion object JQuery {
        fun ajax(parameters: Json): Promise = definedExternally
        fun parseHTML(html: String): Array<HTMLElement>? = definedExternally
    }

    class Promise {
        fun then(callback: Function<Unit>): Unit = definedExternally
        fun done(callback: Function<Unit>): Unit = definedExternally
        fun fail(): Unit = definedExternally
        fun always(): Unit = definedExternally
        fun pipe(): Unit = definedExternally
        fun progress(): Unit = definedExternally
        fun state(): Unit = definedExternally
        fun promise(): Unit = definedExternally
    }

    fun html(s: String): Unit = definedExternally
    fun removeClass(className: String): Unit = definedExternally
    fun addClass(className: String): Unit = definedExternally
    fun height(): Number = definedExternally
    fun height(value: Int): Unit = definedExternally
    fun width(): Number = definedExternally
    fun width(value: Int): Unit = definedExternally
    fun hasClass(className: String): Boolean = definedExternally
    fun text(text: String): Unit = definedExternally
    fun click(): Unit = definedExternally
    fun click(handler: (dynamic) -> Unit): utils.jquery.JQuery = definedExternally
    fun slideUp(): Unit = definedExternally
    fun parent(): utils.jquery.JQuery = definedExternally
    fun attr(name: String, value: String): Unit = definedExternally
    fun append(any: String): Unit = definedExternally

    fun slideDown(): Unit = definedExternally
    fun children(selector: String): utils.jquery.JQuery = definedExternally
    fun outerHeight(includeMargin: Boolean = definedExternally): Int = definedExternally
    fun outerWidth(includeMargin: Boolean = definedExternally): Int = definedExternally
    fun hide(): Unit = definedExternally
    fun show(): Unit = definedExternally
    fun children(): utils.jquery.JQuery = definedExternally
    fun unbind(s: String): Unit = definedExternally
    fun on(s: String, onClose: (Event) -> Unit): Unit = definedExternally
    fun css(key: String, value: dynamic): Unit = definedExternally

    @JsName("val")
    fun value(): String = definedExternally

    @JsName("val")
    fun value(s: String): Unit = definedExternally

    fun circleProgress(options: Json): Unit = definedExternally
    fun on(action: String, selector: String, callback: (event: Event) -> Unit): Unit = definedExternally
    fun keydown(callback: (KeyboardEvent) -> Unit): Unit = definedExternally
    fun find(selector: String): utils.jquery.JQuery = definedExternally
    fun trigger(action: String): Unit = definedExternally
    fun focus(): Unit = definedExternally
    operator fun get(index: Int): Element = definedExternally


    fun toggle(): Unit = definedExternally
    fun resizable(parameters: Json): Unit = definedExternally
    fun resizable(mode: String, param: String, value: Any?): Unit = definedExternally
    fun selectmenu(parameters: Json): Unit = definedExternally
    fun selectmenu(command: String): Unit = definedExternally
    fun tabs(): Unit = definedExternally
    fun button(command: String): utils.jquery.JQuery = definedExternally
    fun button(parameters: Json): utils.jquery.JQuery = definedExternally
    fun dialog(command: String): utils.jquery.JQuery = definedExternally
    fun dialog(parameters: Json): utils.jquery.JQuery = definedExternally
    fun dialog(mode: String, param: String, value: Any?): Unit = definedExternally
    fun accordion(params: Json): utils.jquery.JQuery = definedExternally
    fun accordion(command: String): utils.jquery.JQuery = definedExternally
    fun accordion(command: String, name: String, value: Any): utils.jquery.JQuery = definedExternally
    fun button(mode: String, param: String, value: Any?): utils.jquery.JQuery = definedExternally
    fun tabs(mode: String, param: String, value: Any?): utils.jquery.JQuery = definedExternally

    fun toArray(): Array<HTMLElement> = definedExternally
    @JsName("is")
    fun isCheck(s: String): Boolean
}

@JsName("$")
external fun jq(document: Document): utils.jquery.JQuery = definedExternally

@JsName("$")
external fun jq(selector: String): utils.jquery.JQuery = definedExternally

@JsName("$")
external fun jq(element: Element): utils.jquery.JQuery = definedExternally

@JsName("$")
external fun jq(jq: JQuery): utils.jquery.JQuery = definedExternally