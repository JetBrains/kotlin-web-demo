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

package views.editor

import kotlinx.html.*
import kotlinx.html.js.*
import kotlinx.html.dom.*
import providers.HelpProvider
import utils.ElementPosition
import kotlin.browser.document

object HelpViewForWords{
    private val element = document.body!!.append.div {
        classes = setOf("words-help")
        style = "display: none"
    }

    private val textElement = element.append.div {
        classes = setOf("text")
    }

    public fun show(text: String, pos: ElementPosition) {
        element.style.left = (pos.left + 2).toString() + "px"
        element.style.top = (pos.top + 15).toString() + "px"
        element.style.display = "block"
        textElement.innerHTML = text
    }

    public fun hide() {
        element.style.display = "none"
    }
}