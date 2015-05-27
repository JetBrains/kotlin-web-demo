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

package application

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement
import providers.ConverterProvider
import utils.clear
import views.dialogs.ConverterView
import views.dialogs.Dialog
import kotlin.browser.document

class Application {
    private val converterProvider = ConverterProvider()
    private val converterView = ConverterView(converterProvider)

    public val iframe: HTMLIFrameElement = document.getElementById("k2js-iframe") as HTMLIFrameElement;
    public val iframeDialog: Dialog = Dialog(
            document.getElementById("iframePopup") as HTMLElement,
            width = 640,
            height = 360,
            resizable = false,
            autoOpen = false,
            modal = true,
            onClose = {iframe.clear()}
    )

    init {
        initButtons()
    }

    fun initButtons() {
        val converterButton = document.getElementById("java2kotlin-button") as HTMLElement
        converterButton.onclick = { converterView.open() };
    }
}
