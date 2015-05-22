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

import jquery.jq
import jquery.ui.dialog
import org.w3c.dom.HTMLElement
import kotlin.properties.ReadWriteProperty
import org.w3c.dom.events.Event
import utils.KeyCode
import views.keydown

/**
 * Created by Semyon.Atamas on 5/20/2015.
 */

class Dialog(
        val dialogElement: HTMLElement,
        title: String? = null,
        autoOpen: Boolean = true,
        resizable: Boolean = true,
        modal: Boolean = false,
        minWidth: Int = 150,
        width: Int = 300,
        height: Int? = null,
        buttons: Array<Button> = arrayOf<Button>(),
        onOpen: ((dynamic, dynamic) -> Unit)? = null,
        resizeStop: (() -> Unit)? = null
) {
    init {
        jq(dialogElement).keydown { event ->
            if (event.keyCode == KeyCode.ESCAPE.code) {
                jq(dialogElement).dialog("close");
            }
            event.stopPropagation();
        }

        jq(dialogElement).dialog(json(
                "resizable" to resizable,
                "modal" to modal,
                "width" to width,
                "autoOpen" to autoOpen,
                "open" to onOpen,
                "minWidth" to minWidth,
                "buttons" to buttons,
                "height" to (height ?: "auto"),
                "resizeStop" to resizeStop
        ))
    }

    var title by DialogProperty(title)
    var autoOpen by DialogProperty(autoOpen)
    var resizable by DialogProperty(resizable)
    var minWidth by DialogProperty(minWidth)
    var modal by DialogProperty(modal)
    var height by DialogProperty(height)
    var width by DialogProperty(width)
    var buttons by DialogProperty(buttons)

    fun open() {
        jquery.jq(dialogElement).dialog("open");
    }

    fun close() {
        jquery.jq(dialogElement).dialog("close");
    }
}

class Button(val text: String, val click: (Event)->Unit)

class DialogProperty<T>(initialValue: T) : ReadWriteProperty<Any?, T> {
    private var value: T = initialValue

    override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        var dialog = thisRef as Dialog
        this.value = value
        jq(dialog.dialogElement).dialog("option", desc.name, value)
    }

    override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        return value
    }

}