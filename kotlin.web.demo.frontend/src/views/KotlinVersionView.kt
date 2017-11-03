/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import application.KotlinWrapperConfig
import kotlinx.html.dom.append
import kotlinx.html.option
import org.w3c.dom.HTMLSelectElement
import utils.jquery.jq
import kotlin.js.json
import kotlin.properties.Delegates

class KotlinVersionView(
    val element: HTMLSelectElement,
    val onChange: (String) -> dynamic
) {
    var defaultVersion by Delegates.notNull<String>()
    var select = jq(element)

    init {
        select.selectmenu(json(
            "icons" to json("button" to "selectmenu-arrow-icon"),
            "position" to json("my" to "bottom", "at" to "top")
        ))
        select.on("selectmenuchange", { event ->
            val newValue = (event.currentTarget as HTMLSelectElement).value
            onChange(newValue)
        })
    }

    fun init(kotlinVersions: Array<KotlinWrapperConfig>) {
        kotlinVersions.forEach {
            if (it.latestStable) {
                defaultVersion = it.version
            }
            element.append.option {
                +("v. " + it.version)
                value = it.version
                if (it.latestStable) {
                    selected = true
                }
                disabled = it.obsolete
            }
        }
        select.selectmenu("refresh")
    }

    fun setVersion(newVersion: String?) {
        if (newVersion != null) {
            select.value(newVersion)
        } else {
            select.value(defaultVersion)
        }
        select.selectmenu("refresh")
    }
}