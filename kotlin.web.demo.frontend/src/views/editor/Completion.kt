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

import org.w3c.dom.HTMLElement
import kotlin.browser.document
import html4k.*
import html4k.js.*
import html4k.dom.*
import utils.editor.CodeMirror
import utils.editor.Position

data class Hint(val from: Position, val to: Position, var list: Array<Completion>)

class Completion(private val proposal: CompletionProposal) {
    val text = proposal.text
    val displayText = proposal.displayText

    fun render(element: HTMLElement, self: dynamic, data: dynamic) = element.append {
        div {
            classes = setOf("icon", proposal.icon)
        }
        div {
            +displayText
            classes = setOf("name")
        }
        div {
            +proposal.tail
            classes = setOf("tail")
        }
    }

    fun hint(cm: CodeMirror, self: dynamic, data: dynamic){
        var cur = cm.getCursor()
        val token = cm.getTokenAt(cm.getCursor())
        val replacement: String = data.text

        val from = Position(cur.line, token.start)
        var to = Position(cur.line, token.end)

        if ((token.string == ".") || (token.string == " ") || (token.string == "(")) {
            //Insert string
            cm.replaceRange(replacement, to, to);
        } else {
            cm.replaceRange(replacement, from, to);
            if (data.text.endsWith('(')) {
                cm.replaceRange(")", Position(cur.line, token.start + replacement.length()));
                cm.execCommand("goCharLeft")
            }
        }
    };
}

public data class CompletionProposal(val icon: String, val text: String, val displayText: String, val tail: String)