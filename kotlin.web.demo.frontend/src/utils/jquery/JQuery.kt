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
        fun ajax(parameters: Json): Promise
        fun parseHTML(html: String): Array<HTMLElement>?
    }

    class Promise {
        fun then(callback: Function<Unit>)
        fun done(callback: Function<Unit>)
        fun fail()
        fun always()
        fun pipe()
        fun progress()
        fun state()
        fun promise()
    }

    fun html(s: String)
    fun removeClass(className: String)
    fun addClass(className: String)
    fun height(): Number
    fun height(value: Int)
    fun width(): Number
    fun width(value: Int)
    fun hasClass(className: String): Boolean
    fun text(text: String)
    fun click()
    fun click(handler: Element.(MouseClickEvent ) -> Unit): utils.jquery.JQuery = this;
    fun slideUp()
    fun parent(): utils.jquery.JQuery
    fun attr(name: String, value: String)
}

@native
public fun JQuery.slideDown()

@native
public fun JQuery.height(height: Int)

@native
public fun JQuery.width(width: Int)

@native
public fun JQuery.children(selector: String): JQuery

@native
public fun JQuery.outerHeight(includeMargin: Boolean = false): Int

@native
public fun JQuery.outerWidth(includeMargin: Boolean = false): Int

@native
public fun JQuery.toArray(): Array<HTMLElement>

@native("is")
public fun JQuery.isCheck(s: String): Boolean

@native
fun JQuery.hide()

@native
fun JQuery.show()

@native
fun JQuery.children(): JQuery

@native
fun JQuery.unbind(s: String)

@native
fun JQuery.on(s: String, onClose: (Event) -> Unit)

@native
fun JQuery.css(key: String, value: dynamic)

@native("val")
fun JQuery.value(): String

@native("val")
fun JQuery.value(s: String)

@native("$")
fun jq(document: Document): JQuery

@native("$")
fun jq(selector: String): JQuery

@native("$")
fun jq(element: Element): JQuery

@native("$")
fun jq(jq: JQuery): JQuery

@native
fun JQuery.circleProgress(options: Json)

@native
fun JQuery.on(action: String, selector: String, callback: (event: Event) -> Unit)

@native
fun JQuery.keydown(callback: (KeyboardEvent) -> Unit)

@native
fun JQuery.find(selector: String): JQuery

@native
fun JQuery.trigger(action: String)

@native
fun JQuery.focus()

@native
operator fun JQuery.get(index: Int): Element