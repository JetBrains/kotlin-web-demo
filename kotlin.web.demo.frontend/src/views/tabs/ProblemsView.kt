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

package views.tabs

import kotlinx.html.*
import kotlinx.html.js.*
import kotlinx.html.dom.*
import jquery.jq
import model.File
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import utils.KeyCode
import utils.a11yTree
import utils.jquery.find
import utils.jquery.toArray
import utils.parseBoolean
import utils.unEscapeString
import views.editor.Diagnostic
import kotlin.browser.document


class ProblemsView(
        private val element: HTMLElement,
        private val setCursor: (String, Int, Int) -> Unit
) {

    fun clear() {
        element.innerHTML = ""
    }

    fun addMessages(errorMap: Map<File, Array<Diagnostic>>) {
        val fileNodes = jq("#problems-tree").find(">li").toArray()
        val collapsedNodes = arrayListOf<String>()
        for (fileNode in fileNodes) {
            val expanded = parseBoolean(fileNode.getAttribute("aria-expanded"))
            if (!expanded) collapsedNodes.add(fileNode.id)
        }

        element.innerHTML = ""
        val treeElement = element.append.ul { id = "problems-tree" }

        for ((file, errors) in errorMap) {
            if (errors.isNotEmpty()) renderFileNode(file, errors, treeElement)
        }

        jq(treeElement).a11yTree(json(
                "toggleSelector" to ".tree-node-header .toggle-arrow",
                "treeItemLabelSelector" to ".tree-node-header .text",
                "onFocus" to { item: dynamic ->
                    if (jq(document.activeElement!!).hasClass("tree-node")) {
                        item.focus()
                    }
                }
        ))

        jq("#problems-tree").find("li[aria-expanded]").attr("aria-expanded", "true")
        for (id in collapsedNodes) {
            document.getElementById(id)!!.setAttribute("aria-expanded", "false")
        }
    }

    private fun renderFileNode(file: File, errors: Array<Diagnostic>, parentElement: HTMLElement) {
        val nodeElement = parentElement.append.li {
            classes = setOf("tree-node")
            id = file.id + "_problems"
            tabIndex = "-1"
            div {
                classes = setOf("tree-node-header", "file-problems-header")
                div { classes = setOf("toggle-arrow") }
                div { classes = setOf("icon", "kotlin-file-icon") }
                div {
                    +file.name
                    classes = setOf("text")
                }
            }
        }
        val childrenElement = nodeElement.append.ul {}
        for (error in errors) {
            renderErrorNode(file, error, childrenElement)
        }
    }

    private fun renderErrorNode(file: File, diagnostic: Diagnostic, parentElement: HTMLElement) = parentElement.append.li {
        classes = setOf("tree-node")
        tabIndex = "-1"
        div {
            classes = setOf("tree-node-header")
            val severity = diagnostic.severity.toLowerCase()
            div { classes = setOf("icon", severity) }
            div {
                +(severity.capitalize() +
                        ":(" + (diagnostic.interval.start.line + 1) + ", " + diagnostic.interval.start.ch + ") " +
                        unEscapeString(diagnostic.message))
                classes = setOf("text")
            }
        }
        onDoubleClickFunction = {
            setCursor(file.name, diagnostic.interval.start.line, diagnostic.interval.start.ch)
        }
        onKeyUpFunction = fun(event: Event) {
            if (event !is KeyboardEvent || event.keyCode != KeyCode.ENTER.code) return
            setCursor(file.name, diagnostic.interval.start.line, diagnostic.interval.start.ch)
        }
    }
}

