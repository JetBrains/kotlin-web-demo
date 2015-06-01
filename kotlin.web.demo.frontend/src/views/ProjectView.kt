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

import addKotlinExtension
import application.app
import fileProvider
import jquery.jq
import model.File
import model.Project
import model.ProjectType
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import projectProvider
import utils.editor
import utils.getFileIdFromUrl
import utils.slideDown
import views.dialogs.InputDialogView
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass

class ProjectView(
        val header: ProjectHeader,
        val contentElement: HTMLDivElement,
        val headerElement: HTMLDivElement,
        private val parent: FolderView,
        private val onDelete: () -> Unit,
        private val onHeaderClick: (String) -> Unit,
        private val onSelected: (ProjectView) -> Unit
) {
    val depth = parent.depth + 1
    var nameSpan: HTMLSpanElement
    var project: Project = initProject()
    val fileViews = hashMapOf<String, FileView>()
    var selectedFileView: FileView? = null

    init {
        headerElement.id = header.publicId;

        jq(contentElement).slideUp();
        headerElement.className = "examples-project-name";
        headerElement.setAttribute("depth", depth.toString());
        var img = document.createElement("div");
        img.className = "icon";
        headerElement.appendChild(img);
        headerElement.onclick = {
            onHeaderClick(header.publicId);
        };

        nameSpan = document.createElement("span") as HTMLSpanElement;
        nameSpan.className = "file-name-span";
        nameSpan.style.cursor = "pointer";
        nameSpan.innerHTML = header.name;
        nameSpan.title = nameSpan.innerHTML;
        headerElement.appendChild(nameSpan);

        var actionIconsElement = document.createElement("div");
        actionIconsElement.className = "icons";
        headerElement.appendChild(actionIconsElement);

        if (header.type == ProjectType.USER_PROJECT) {
            var addFileImg = document.createElement("div") as HTMLDivElement;
            addFileImg.className = "new-file icon";
            addFileImg.onclick = { event ->
                event.stopPropagation();
                InputDialogView.open(
                        "Add new file",
                        "File name:",
                        "Add",
                        "Untitled",
                        { name -> validateNewFileName(name) },
                        { name -> fileProvider.addNewFile(this.project, name) }
                );
            };
            actionIconsElement.appendChild(addFileImg);

            var renameImg = document.createElement("div") as HTMLDivElement;
            renameImg.className = "rename icon";
            renameImg.title = "Rename this project";
            renameImg.onclick = { event ->
                event.stopPropagation();
                InputDialogView.open(
                        "Rename project",
                        "Project name:",
                        "Rename",
                        project.name,
                        { newName ->
                            if (project.name == newName) {
                                ValidationResult(valid = true);
                            } else {
                                app.accordion.validateNewProjectName(newName);
                            }
                        },
                        { newName -> projectProvider.renameProject(project, newName) }
                );
            };
            actionIconsElement.appendChild(renameImg);
        }

        if (header.type == ProjectType.USER_PROJECT || header.type == ProjectType.PUBLIC_LINK) {
            var deleteButton = document.createElement("div") as HTMLDivElement;
            deleteButton.className = "delete icon";
            deleteButton.title = "Delete this project";
            deleteButton.onclick = { event ->
                if (window.confirm("Delete project " + header.name + "?")) {
                    projectProvider.deleteProject(header.publicId, header.type, {delete()});
                }
                event.stopPropagation();
            };
            actionIconsElement.appendChild(deleteButton);
        }

        if (header.type != ProjectType.USER_PROJECT) {
            if (localStorage.getItem(header.publicId) != null) {
                headerElement.addClass("modified");
            }

            var revertIcon = document.createElement("div") as HTMLDivElement;
            revertIcon.className = "revert icon";
            revertIcon.title = "Revert this project";
            actionIconsElement.appendChild(revertIcon);

            revertIcon.onclick = {
                project.loadOriginal();
            };

            project.revertibleListener.addModifyListener { event ->
                if(!event.newValue)
                    revertIcon.parentNode!!.removeChild(revertIcon);
            }
        }
    }

    fun validateNewFileName(fileName: String): ValidationResult {
        if (fileName == "") {
            return ValidationResult(false, "File name can't be empty");
        }
        if (fileName.size >= 95) {
            return ValidationResult(false, "File name is too long")
        }
        if (!fileName.matches("^[a-zA-Z0-9,_\\- ]+$")) {
            return ValidationResult(false, "File name can contain only the following characters:" +
                    "<span style=\"font-family: monospace\"> a-z A-Z 0-9 ' ' ',' '_' '-'</span>");
        }
        val fileNameWithExtension = addKotlinExtension(fileName);
        for (fileView in fileViews.values()) {
            if (fileView.file.name == fileName) {
                return ValidationResult(
                        false,
                        "File with this name already exists in the project"
                );
            }
        }
        return ValidationResult(true);
    }

    fun initProject(): Project {
        val project = Project(
                header.type,
                header.publicId,
                header.name,
                parent,
                onFileAdded = { file: File ->
                    var fileView = createFileView(file);
                    fileViews[file.id] = fileView;
                    selectedFileView = fileView;
                    if (isSelected()) {
                        fileView.fireSelectEvent();
                    }
                },
                onFileDeleted = { publicId ->
                    if (selectedFileView!!.file.id == publicId) {
                        app.accordion.selectedFileDeleted();
                        selectedFileView = null;
                    }
                    fileViews.remove(publicId);
                    if (!project.files.isEmpty()) {
                        selectFirstFile();
                    }
                },
                onContentLoaded = { files ->
                    contentElement.innerHTML = "";

                    nameSpan.innerHTML = project.name;
                    nameSpan.title = nameSpan.innerHTML;

                    for (file in files) {
                        fileViews[file.id] = createFileView(file);
                    }

                    if (!files.isEmpty()) {
                        selectedFileView = getFileFromUrl() ?: fileViews[files[0].id];

                        if (app.accordion.selectedProjectView!!.project === project) {
                            selectedFileView!!.fireSelectEvent();
                            onSelected(this);
                        }
                    } else if (app.accordion.selectedProjectView!!.project === project) {
                        onSelected(this);
                        editor.closeFile();
                    }
                },
                onContentNotFound = {
                    if (project.type == ProjectType.PUBLIC_LINK) {
                        window.alert("Can't find project origin, maybe it was removed by the user.");
                        project.revertible = false;
                        if (!project.contentLoaded) {
                            delete();
                        }
                    }
                }
        )

        project.modifiedListener.addModifyListener { event ->
            if (event.newValue) {
                headerElement.addClass("modified");
            } else {
                headerElement.removeClass("modified");
            }
        }

        project.nameListener.addModifyListener { event ->
            val newName = event.newValue
            nameSpan.innerHTML = newName;
            nameSpan.title = nameSpan.innerHTML;
            if (isSelected()) {
                navBarView.onSelectedProjectRenamed(newName);
            }
        };
        return project
    }

    fun select() {
        parent.select();
        headerElement.className += " selected";
        jq(contentElement).slideDown();
        if (!project.contentLoaded) {
            project.loadContent(false);
        } else {
            if (selectedFileView != null) {
                selectedFileView!!.fireSelectEvent();
            } else {
                if (!project.files.isEmpty()) {
                    selectFirstFile();
                }
            }
            onSelected(this);
        }
    }

    fun selectFileFromUrl() = getFileFromUrl()?.fireSelectEvent()

    private fun getFileFromUrl() = fileViews.get(getFileIdFromUrl())

    fun selectFirstFile() {
        selectedFileView = fileViews[project.files[0].id];
        selectedFileView!!.fireSelectEvent();
    }

    fun delete(){
        if(parent is MyProgramsFolderView)
            parent.removeProject(this);
        headerElement.parentNode!!.removeChild(headerElement);
        contentElement.parentNode!!.removeChild(contentElement);
        onDelete();
    }

    private fun createFileView(file: File) = FileView(this, contentElement, file);

    private fun isSelected(): Boolean {
        return app.accordion.selectedProjectView!!.project === project;
    }

    fun getFileViewByName(name: String) = fileViews.values().first {
        it.file.name == name
    }
}

data
class ProjectHeader(val name: String, val publicId: String, val type: ProjectType)