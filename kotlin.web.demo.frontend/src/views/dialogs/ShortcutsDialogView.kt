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

package views.dialogs

import KeyCode
import html4k.*
import html4k.dom.*
import html4k.js.*
import jquery.jq
import jquery.ui.dialog
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.KeyboardEvent
import views.keydown
import kotlin.browser.document

object ShortcutsDialogView {
    val dialogElement = document.body!!.append.div {
        title = "Help"
    }

    val shortcutsHelpElement = dialogElement.append.div {
        id = "shortcuts-help"
    }

    val dialog = Dialog(
            dialogElement,
            resizable = false,
            minWidth = 500,
            autoOpen = false,
            modal = true
    )

    init {
        jq(dialogElement).keydown({ event ->
            when (event.keyCode) {
                KeyCode.ENTER.code, KeyCode.ESCAPE.code -> jq(dialogElement).dialog("close");
            }
            event.stopPropagation();
        }) ;
    }

    fun open() {
        dialog.open()
    }

    fun addShortcut(keyNames: Array<String>, description: String) {
        val shortcutElement = shortcutsHelpElement.append.tr {}
        shortcutElement.appendChild(createKeyCombinationElement(keyNames))
        shortcutElement.append.td {
            div {
                +description
                classes = setOf("shortcutDescription")
            }
        }
    }

    private fun createKeyCombinationElement(keyNames: Array<String>): HTMLElement{
        val wrapper = document.create.td {
            classes = setOf("shortcutKeyCombinationWrapper")
        }
        val shortcutKeyCombinationElement = wrapper.append.div{
            classes = setOf("shortcutKeyCombination")
        }

        for (keyName in  keyNames) {
            shortcutKeyCombinationElement.append {
                div {
                    +keyName
                    classes = setOf("shortcutKeyName")
                }
                div {
                    + "+"
                    classes = setOf("shortcutSeparator")
                }
            }
        }
        return wrapper
    }
}