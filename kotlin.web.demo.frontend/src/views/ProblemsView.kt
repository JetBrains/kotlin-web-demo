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

package views

import application.Application
import jquery.JQuery
import jquery.jq
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.KeyboardEvent
import utils.KeyCode
import utils.parseBoolean
import utils.toArray
import utils.unEscapeString
import kotlin.browser.document


class ProblemsView(
        private val element: HTMLElement,
        tabs: JQuery,
        private val setCursor: (String, Int, Int) -> Unit
) {

    fun clear() {
        element.innerHTML = "";
    }

    fun addMessages() {
        var fileNodes = jq("#problems-tree").find(">li").toArray();
        var expandedStatus = hashMapOf<String, Boolean>();
        for (fileNode in fileNodes) {
            expandedStatus.set(fileNode.id, parseBoolean(fileNode.getAttribute("aria-expanded")!!))
        }

        element.innerHTML = "";
        var treeElement = document.createElement("ul") as HTMLLIElement;
        treeElement.id = "problems-tree";
        element.appendChild(treeElement);

        var project = Application.accordion.selectedProjectView!!.project;
        for (file in project.files) {
            if (!file.errors.isEmpty()) {
                var fileId = file.name.replace(" ", "%20") + "_problems";
                var fileProblemsNode: dynamic = json(
                        "children" to arrayOf<dynamic>(),
                        "name" to file.name,
                        "icon" to "kotlin-file-icon",
                        "id" to fileId
                );

                for (error in file.errors) {
                    var errorNode = json(
                            "children" to arrayOf<dynamic>(),
                            "name" to error.severity.toLowerCase().capitalize() + ":(" + (error.interval.start.line + 1) + ", " + error.interval.start.ch + ") " + unEscapeString(error.message),
                            "icon" to error.severity,
                            "id" to fileId + "_" + error.severity + "_" + error.interval.start.line + "_" + error.interval.start.ch,
                            "filename" to file.name,
                            "line" to error.interval.start.line,
                            "ch" to error.interval.start.ch
                    );
                    fileProblemsNode.children.push(errorNode);
                }
                displayTreeNode(fileProblemsNode, treeElement);
            }
        }

        jq(treeElement).a11yTree(json(
                "toggleSelector" to ".tree-node-header .toggle-arrow",
                "treeItemLabelSelector" to ".tree-node-header .text",
                "onFocus" to { item: dynamic ->
                    if (jq(document.activeElement!!).hasClass("tree-node")) {
                        item.focus();
                    }
                }
        ));

        jq("#problems-tree").find("li[aria-expanded]").attr("aria-expanded", "true");
        for (id in expandedStatus.keySet()) {
            if (expandedStatus[id] == false) {
                document.getElementById(id)!!.setAttribute("aria-expanded", "false");
            }
        }
    }

    private fun displayTreeNode(node: dynamic, parentElement: HTMLElement) {
        var nodeElement = document.createElement("li") as HTMLLIElement;
        nodeElement.className = "tree-node";
        nodeElement.id = node.id;
        nodeElement.setAttribute("tabindex", "-1");
        if (node.expanded == "true") {
            nodeElement.setAttribute("aria-expanded", "true");
        }
        parentElement.appendChild(nodeElement);

        var nodeElementHeader = document.createElement("div");
        nodeElementHeader.className = "tree-node-header";
        nodeElement.appendChild(nodeElementHeader);

        var img = document.createElement("div");
        img.className = "icon " + node.icon.toLowerCase();
        nodeElementHeader.appendChild(img);

        var text = document.createElement("div");
        text.className = "text";
        text.textContent = node.name;
        nodeElementHeader.appendChild(text);

        if (node.children.length > 0) {
            nodeElementHeader.className += " file-problems-header";
            var toggle = document.createElement("div");
            toggle.className = "toggle-arrow";
            nodeElementHeader.insertBefore(toggle, nodeElementHeader.firstChild);

            var childrenNode = document.createElement("ul") as HTMLUListElement;
            for (child in node.children) {
                displayTreeNode(child, childrenNode);
            }
            nodeElement.appendChild(childrenNode);
        } else {
            nodeElement.ondblclick = {
                setCursor(node.filename, node.line, node.ch);
            };
            nodeElement.onkeyup = { event ->
                if(event is KeyboardEvent) {
                    if (event.keyCode == KeyCode.ENTER.code) {
                        event.stopPropagation();
                        setCursor(node.filename, node.line, node.ch);
                    }
                }
            };
        }
    }
};