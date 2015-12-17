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

package views.tree

import application.Application
import model.File
import model.Project
import model.UserProject
import org.w3c.dom.HTMLDivElement
import views.dialogs.InputDialogView
import views.dialogs.ValidationResult
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass


public class UserProjectView(
        header: ProjectHeader,
        parent: FolderView,
        onHeaderClick: (ProjectView) -> Unit,
        onSelected: (ProjectView) -> Unit
) : ProjectView(header, parent, onHeaderClick, onSelected) {
    init {
        if (project is UserProject) {
            var renameImg = document.createElement("div") as HTMLDivElement
            renameImg.className = "rename icon"
            renameImg.title = "Rename this project"
            renameImg.onclick = { event ->
                event.stopPropagation()
                InputDialogView(
                        "Rename project",
                        "Project name:",
                        "Rename",
                        project.name,
                        { newName ->
                            if (project.name == newName) {
                                ValidationResult(valid = true)
                            } else {
                                Application.accordion.validateNewProjectName(newName)
                            }
                        },
                        { userInput -> Application.projectProvider.renameProject(project, userInput.value) }
                )
            }
            actionIconsElement.insertBefore(renameImg, actionIconsElement.firstChild)

            var addFileImg = document.createElement("div") as HTMLDivElement
            addFileImg.className = "new-file icon"
            addFileImg.onclick = { event ->
                event.stopPropagation()
                InputDialogView(
                        "Add new file",
                        "File name:",
                        "Add",
                        "Untitled",
                        { name -> project.validateNewFileName(name) },
                        { userInput -> Application.fileProvider.addNewFile(this.project, userInput.value) }
                )
            }
            actionIconsElement.insertBefore(addFileImg, actionIconsElement.firstChild)

        }
    }

    override fun initProject(header: ProjectHeader): Project {
        val project = UserProject(
                header.publicId,
                header.name,
                parent.content,
                header.type,
                onFileDeleted,
                onContentLoaded,
                onContentNotFound,
                { file: File ->
                    var fileView = createFileView(file)
                    fileViews[file.id] = fileView
                    if (isSelected()) {
                        fileView.fireSelectEvent()
                    }
                }
        )
        project.nameListener.addModifyListener { event ->
            val newName = event.newValue
            nameSpan.innerHTML = newName
            nameSpan.title = nameSpan.innerHTML
            if (isSelected()) {
                Application.navBarView.onSelectedProjectRenamed(newName)
            }
        }
        project.modifiedListener.addModifyListener { event ->
            if (event.newValue) {
                headerElement.addClass("modified")
            } else {
                headerElement.removeClass("modified")
            }
        }
        return project
    }
}