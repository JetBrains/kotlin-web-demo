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

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document


fun makeReference(fileName: String?, lineNo: Int): HTMLElement {
    val selectedProjectView = Application.accordion.selectedProjectView!!
    if (fileName != null &&
            selectedProjectView.getFileViewByName(fileName) != null) {
        val fileView = selectedProjectView.getFileViewByName(fileName)
        val a = document.createElement("div") as HTMLDivElement
        a.className = "link"
        a.innerHTML = fileName + ':' + lineNo
        a.onclick = {
            fileView!!.fireSelectEvent()
            Application.editor.setCursor(lineNo - 1, 0)
            Application.editor.focus()
        }
        return a
    } else {
        val span = document.createElement("span") as HTMLSpanElement
        if (fileName != null) {
            span.innerHTML = fileName + ':' + lineNo
        } else {
            span.innerHTML = "Unknown Source"
        }
        return span
    }
}