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

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import utils.KeyCode
import utils.jquery.jq
import utils.jquery.keydown
import kotlin.properties.ReadWriteProperty

class Dialog(
        val dialogElement: HTMLElement,
        title: String? = null,
        autoOpen: Boolean = true,
        resizable: Boolean = true,
        modal: Boolean = false,
        minWidth: Int = 150,
        width: Int = 300,
        height: Int? = null,
        minHeight: Int = 150,
        buttons: Array<DialogButton> = arrayOf<DialogButton>(),
        onOpen: ((dynamic, dynamic) -> Unit)? = null,
        onClose: (() -> Unit)? = null,
        resizeStop: (() -> Unit)? = null
) {
    init {
        jq(dialogElement).keydown { event ->
            if (event.keyCode == KeyCode.ESCAPE.code) {
                jq(dialogElement).dialog("close")
            }
            event.stopPropagation()
        }

        jq(dialogElement).dialog(json(
                "resizable" to resizable,
                "modal" to modal,
                "width" to width,
                "autoOpen" to autoOpen,
                "open" to onOpen,
                "close" to onClose,
                "minWidth" to minWidth,
                "buttons" to buttons,
                "height" to (height ?: "auto"),
                "minHeight" to minHeight,
                "resizeStop" to resizeStop
        ))
    }

    var title by DialogProperty(title)
    var autoOpen by DialogProperty(autoOpen)
    var resizable by DialogProperty(resizable)
    var minWidth by DialogProperty(minWidth)
    var modal by DialogProperty(modal)
    var height by DialogProperty(height)
    var minHeight by DialogProperty(minHeight)
    var width by DialogProperty(width)
    var buttons by DialogProperty(buttons)


    fun open() {
        jq(dialogElement).dialog("open")
    }

    fun close() {
        jq(dialogElement).dialog("close")
    }
}

class DialogButton(val text: String, val click: (Event)->Unit)

class DialogProperty<T>(initialValue: T) : ReadWriteProperty<Any?, T> {
    private var value: T = initialValue

    override fun set(thisRef: Any?, property: PropertyMetadata, value: T) {
        var dialog = thisRef as Dialog
        this.value = value
        jq(dialog.dialogElement).dialog("option", property.name, value)
    }

    override fun get(thisRef: Any?, property: PropertyMetadata): T {
        return value
    }

}