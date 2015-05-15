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

import org.w3c.dom.HTMLDivElement
import projectProvider
import kotlin.browser.document

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
        if (name.length() >= 95)
            return ValidationResult(false, "Project name is too long");
        if (!name.matches("^[a-zA-Z0-9,_\\- ]+$"))
            return ValidationResult(false, "Project name can contain only the following characters:" +
                    "<span style=\"font-family: monospace\"> a-z A-Z 0-9 ' ' ',' '_' '-'</span>")
        for (projectView in projects)
            if (projectView.getProjectData().getName() == name) {
                return ValidationResult(false, "Project with that name already exists");
            }
        return ValidationResult(true);
    }

    fun addNewProject(project: ProjectView){
        projects.add(project)
    }

    //TODO replace with more global function removeChild
    fun removeProject(project: ProjectView){
        projects.remove(project)
    }
}