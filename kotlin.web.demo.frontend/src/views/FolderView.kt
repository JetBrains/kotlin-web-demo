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

import jq
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document

/**
 * Created by Semyon.Atamas on 4/6/2015.
 */

//TODO remove addProject function
open class FolderView(parentNode: HTMLDivElement,
                      content: dynamic,
                      val parent: FolderView?,
                      val addProject: (HTMLDivElement, dynamic, FolderView) -> ProjectView) {
    val depth: Int = if (parent == null) 0 else parent.depth + 1;
    val name: String = content.name
    val projects = arrayListOf<ProjectView>()
    val childFolders = arrayListOf<FolderView>()
    val headerElement = document.createElement("div") as HTMLDivElement
    val contentElement = document.createElement("div") as HTMLDivElement
    protected val folderNameElement: HTMLDivElement = document.createElement("div") as HTMLDivElement;

    init {
        headerElement.className = "folder-header";
        headerElement.setAttribute("depth", depth.toString())
        headerElement.id = content.id
        parentNode.appendChild(headerElement);

        folderNameElement.textContent = name
        folderNameElement.className = "text"
        headerElement.appendChild(folderNameElement)

        parentNode.appendChild(contentElement)

        for (projectHeader in content.projects) {
            projects.add(addProject(contentElement, projectHeader, this))
        }

        for (folderContent in content.childFolders) {
            childFolders.add(FolderView(contentElement, folderContent, this, addProject))
        }

        if (!childFolders.isEmpty()) {
            jq(contentElement).accordion(object {
                val heightStyle = "content"
                val navigation = true
                val active = 0
                val icons = object {
                    val activeHeader = "examples-open-folder-icon"
                    val header = "examples-closed-folder-icon"
                }
            });
        }
    }

    fun select(){
        parent?.select()
        headerElement.click()
    }
}