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

import FileType
import addKotlinExtension
import application.app
import fileProvider
import html4k.dom.append
import html4k.dom.create
import html4k.js.div
import html4k.js.onClickFunction
import model.File
import model.ProjectType
import org.w3c.dom.HTMLElement
import removeKotlinExtension
import utils.unEscapeString
import views.dialogs.InputDialogView
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass

class FileView(val projectView: ProjectView, parentNode: HTMLElement, val file: File) {
    var onSelect: (() -> Unit)? = null
    private val depth = projectView.depth + 1
    val wrapper = parentNode.append.div {
        classes = setOf("file-header-wrapper")
        attributes.set("depth", depth.toString())
    }
    val headerElement = wrapper.append.div {
        classes = setOf("file-header")
    }

    fun fireSelectEvent() {
        projectView.selectedFileView = this;
        app.accordion.selectFile(this);
    }

    //TODO remove getHeaderText and updateName
    fun getHeaderText(): String{
        return fileNameElement.innerHTML;
    }

    fun updateName(){
        fileNameElement.innerHTML = file.name;
        fileNameElement.title = fileNameElement.innerHTML;
    }

    val fileNameElement = document.create.div {
        +file.name
        title = file.name
        classes = setOf("text")
    }

    init {
        file.listenableIsModified.addModifyListener({ e ->
            if (e.newValue) {
                headerElement.addClass("modified");
            } else {
                headerElement.removeClass("modified");
            }
            if (isSelected()) {
                app.accordion.onModifiedSelectedFile(file);
            }
        });

        file.listenableName.addModifyListener({ e ->
            fileNameElement.innerHTML = e.newValue;
            fileNameElement.title = fileNameElement.innerHTML;
        });

        var icon = headerElement.append.div {
            classes = setOf("icon")
        }

        when (file.type) {
            FileType.KOTLIN_FILE -> icon.addClass("kotlin");
            FileType.KOTLIN_TEST_FILE -> icon.addClass("kotlin-test");
            FileType.JAVA_FILE -> icon.addClass("java");
        }

        if (!file.isModifiable) {
            headerElement.addClass("unmodifiable");
            icon.addClass("unmodifiable")
        }

        headerElement.appendChild(fileNameElement)

        var actionIconsElement = headerElement.append.div {
            classes = setOf("icons")
        }

        if (projectView.project.type == ProjectType.USER_PROJECT) {
            if (file.isModifiable) {
                actionIconsElement.append.div {
                    classes = setOf ("rename", "icon")
                    title = "Rename file"
                    onClickFunction = { event ->
                        InputDialogView.open(
                                title = "Rename file",
                                inputLabel = "File name:",
                                okButtonCaption = "Rename",
                                defaultValue = removeKotlinExtension(file.name),
                                validate = { newName ->
                                    if (removeKotlinExtension(file.name) == newName) {
                                        ValidationResult(true);
                                    } else {
                                        projectView.validateNewFileName(newName);
                                    }
                                },
                                callback = { newName: String ->
                                    fileProvider.renameFile(file.id, { newName: String ->
                                        file.name = addKotlinExtension(newName);
                                    }, newName);
                                }
                        );
                        event.stopPropagation();
                    }
                }
            }

            actionIconsElement.append.div {
                title = "Delete this file"
                classes = setOf("delete", "icon")
                onClickFunction = { event ->
                    if (window.confirm("Delete file " + file.name)) {
                        fileProvider.deleteFile(file, {
                            file.project.deleteFile(file);
                            headerElement.parentNode!!.removeChild(headerElement);
                        });
                    }
                    event.stopPropagation();
                }
            }
        } else if (file.isRevertible) {
            var revertIcon = actionIconsElement.append.div {
                classes = setOf("revert", "icon")
                title = "Revert this file"
                onClickFunction = {
                    fileProvider.loadOriginalFile(
                            file,
                            { content: dynamic ->
                                file.text = content.text;
                                file.originalText = content.text;
                                file.name = unEscapeString(content.name);
                                file.changesHistory = null;
                            },
                            {
                                window.alert("Can't find file origin, maybe it was removed by a user");
                                file.isRevertible = false;
                            }
                    );
                }
            }

            file.listenableIsRevertible.addModifyListener({
                revertIcon.parentNode!!.removeChild(revertIcon);
            })
        }

        headerElement.onclick = {fireSelectEvent()};
    }

    private fun isSelected(): Boolean {
        return app.accordion.selectedFileView == this;
    }


}