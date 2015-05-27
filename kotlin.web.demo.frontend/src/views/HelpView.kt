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

import kotlin.browser.document
import html4k.*
import html4k.js.*
import html4k.dom.*
import providers.HelpProvider

/**
 * Created by Semyon.Atamas on 5/18/2015.
 */

class HelpView(private val model: HelpProvider) {

    fun setPosition(pos: dynamic) {
        element.style.left = pos.left + 2 + "px";
        element.style.top = pos.top + 15 + "px";
    }

    fun update(name: String) {
        var text = model.getHelpForWord(name);
        if (text != null) {
            setText(text);
        } else {
            hide();
        }
    }

    fun hide() {
        element.style.display = "none"
    }


    var element = document.body!!.append.div {
        classes = setOf("words-help")
    }

    var textElement = element.append.div {
        classes = setOf("text")
    }

    fun setText(text: String?) {
        element.style.display = "block";
        if (text != null) {
            textElement.innerHTML = text;
        }
    }

}