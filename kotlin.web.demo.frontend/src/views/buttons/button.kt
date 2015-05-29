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
 * distributed under the License is distributed utils.utils.on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.buttons

import jquery.jq
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import utils.KeyCode
import views.dialogs.Dialog
import views.keydown
import kotlin.properties.ReadWriteProperty

class Button(
        val buttonElement: HTMLElement,
        disabled: Boolean = false
) {
    init {
        jq(buttonElement).button(json(
                "disabled" to disabled
        ))
    }

    public var disabled: Boolean by ButtonProperty(disabled)
}

private class ButtonProperty<T>(initialValue: T) : ReadWriteProperty<Any?, T> {
    private var value: T = initialValue

    override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        var button = thisRef as Button
        this.value = value
        jq(button.buttonElement).button("option", desc.name, value)
    }

    override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        return value
    }

}