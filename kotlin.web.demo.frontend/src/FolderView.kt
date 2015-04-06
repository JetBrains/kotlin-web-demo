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

import kotlin.dom.createElement
import kotlin.js.dom.html.Event
import kotlin.js.dom.html.HTMLDivElement
import kotlin.js.dom.html.document

/**
 * Created by Semyon.Atamas on 4/6/2015.
 */

//TODO remove addProject function
open class FolderView(parentNode: HTMLDivElement,
                      content: dynamic,
                      val parent: FolderView?,
                      val addProject: (HTMLDivElement, dynamic, FolderView) -> ProjectView) {
    val depth: Int = if (parent == null) 0 else parent.depth + 1;
    val name = content.name: String
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

native
val projectProvider: dynamic = noImpl

data class ValidationResult(val valid: Boolean, val message: String = "")

class MyProgramsFolderView(parentNode: HTMLDivElement,
                           content: dynamic,
                           parent: FolderView?,
                           addProject: (HTMLDivElement, dynamic, FolderView) -> ProjectView) :
        FolderView(parentNode, content, parent, addProject) {

    val newProjectDialog = ({
        val value = InputDialogView("Add new project", "Project name:", "Add")
        value.validate = { name -> validateNewProjectName(name) }
        value
    })()

    init {
        if (!loginView.isLoggedIn()) {
            folderNameElement.style.setProperty("display", "inline-block", "");
            headerElement.style.setProperty("color", "rgba(0,0,0,0.5)", "");
            var login_link: HTMLDivElement = document.createElement("div") as HTMLDivElement;
            login_link.id = "login-link";
            login_link.className = "login-link";
            login_link.innerHTML = "(please log in)";
            headerElement.onclick = {
                loginView.openLoginDialog();
            };
            headerElement.appendChild(login_link);
        } else {
            var actionIconsElement = document.createElement("div") as HTMLDivElement;
            actionIconsElement.className = "icons";
            headerElement.appendChild(actionIconsElement);

            var newProjectButton = document.createElement("div") as HTMLDivElement;
            newProjectButton.className = "new-project icon";
            newProjectButton.onclick = {
                newProjectDialog.open(projectProvider.addNewProject, "Untitled");
            };
            actionIconsElement.appendChild(newProjectButton);
        }
    }

    fun validateNewProjectName(name: String): ValidationResult {
        if (name == "")
            return ValidationResult(false, "Project name can't be empty");
        if (!name.matches("^[a-zA-Z0-9,_\\- ]+$"))
            return ValidationResult(false, "Project name can contain only the following characters:" +
                    "<span style=\"font-family: monospace\"> a-z A-Z 0-9 ' ' ',' '_' '-'</span>")
        for (projectView in projects)
            if (projectView.getProjectData().getName() == name) {
                return ValidationResult(false, "Project with that name already exists");
            }
        return ValidationResult(true);
    }
}