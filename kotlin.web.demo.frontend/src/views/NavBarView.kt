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

import File
import Project
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node
import kotlin.browser.document

/**
 * Created by Semyon.Atamas on 3/31/2015.
 */

class NavBarView(private val navigationElement: HTMLDivElement) {
    fun onFileSelected(oldFile: File?, newFile: File) {
        navigationElement.innerHTML = "";
        val navItem = createNavItem(newFile.name);
        navigationElement.appendChild(navItem);
        createNavItem(newFile.project)

        oldFile?.listenableName?.removeNotifyListener("navBarListener");
        newFile.listenableName.addModifyListener("navBarListener", { e ->
            navItem.textContent = e.newValue
        })
    }

    fun onProjectSelected(project: Project){
        navigationElement.innerHTML = "";
        createNavItem(project);
    }

    fun onSelectedFileDeleted(){
        navigationElement.removeChild(navigationElement.lastChild as Node);
    }

    fun onSelectedProjectRenamed(newName: String){
        //TODO
        var navItem = navigationElement.childNodes.item(1) as HTMLDivElement
        navItem.textContent = newName
    }

    private fun createNavItem(project: Project){
        navigationElement.insertBefore(createNavItem(project.getName()), navigationElement.firstChild)
        var folder: FolderView? = project.getParent()
        while (folder != null){
            navigationElement.insertBefore(createNavItem(folder.name), navigationElement.firstChild)
            folder = folder.parent
        }
    }

    private fun createNavItem(name: String): HTMLDivElement {
        val navItem = document.createElement("div") as HTMLDivElement;
        navItem.className = "grid-nav-item";
        navItem.textContent = name;
        return navItem
    }
}