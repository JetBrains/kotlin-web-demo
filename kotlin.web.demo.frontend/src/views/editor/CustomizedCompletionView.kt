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

import html4k.dom.append
import html4k.js.div
import org.w3c.dom.HTMLElement
import providers.CompletionProposal
import utils.codemirror.CodeMirror
import utils.codemirror.CompletionView
import utils.codemirror.Position

internal class CustomizedCompletionView(private val proposal: CompletionProposal): CompletionView {
    override val text = proposal.text
    override val displayText = proposal.displayText

    override fun render(element: HTMLElement, self: dynamic, data: dynamic) {
        element.append {
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
    }

    override fun hint(cm: CodeMirror, self: dynamic, data: dynamic) {
        var cur = cm.getCursor()
        val token = cm.getTokenAt(cm.getCursor())

        val from = Position(cur.line, token.start)
        var to = Position(cur.line, token.end)

        if ((token.string == ".") || (token.string == " ") || (token.string == "(")) {
            //Insert string
            cm.replaceRange(text, to)
        } else {
            cm.replaceRange(text, from, to)
            if (text.endsWith('(')) {
                cm.replaceRange(")", Position(cur.line, token.start + text.length()))
                cm.execCommand("goCharLeft")
            }
        }
    }
}
