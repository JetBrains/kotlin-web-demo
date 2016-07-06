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

package views.dialogs

import kotlinx.html.dom.append
import kotlinx.html.iframe
import kotlinx.html.js.div
import org.w3c.dom.HTMLIFrameElement
import utils.jquery.ui.Dialog
import kotlin.browser.document
import kotlin.dom.clear

class IframeDialog(kotlinVersion: String) {
    private val element = document.body!!.append.div {
        classes = setOf("iframePopup")
        title = "Canvas example"
    }

    val iframe = element.append.iframe {
        src = "/static/kotlin/$kotlinVersion/iframe.html"
        classes = setOf("k2js-iframe")
    } as HTMLIFrameElement

    private val dialog = Dialog(
            element,
            width = 640,
            height = 360,
            resizable = false,
            autoOpen = false,
            modal = true,
            onClose = { iframe.clear() }
    )

    fun open() {
        dialog.open()
    }
}