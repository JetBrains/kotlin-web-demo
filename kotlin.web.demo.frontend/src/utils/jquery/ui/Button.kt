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

package utils.jquery.ui

import jquery.jq
import org.w3c.dom.HTMLElement
import utils.jquery.ui.button
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        var button = thisRef as Button
        this.value = value
        jq(button.buttonElement).button("option", property.name, value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

}