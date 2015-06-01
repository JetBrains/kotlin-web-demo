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

package providers

import checkDataForException
import checkDataForNull
import generateAjaxUrl
import model.Project
import model.ProjectType
import utils.ActionStatusMessages
import utils.blockContent
import utils.unBlockContent
import views.ActionStatusMessage

class ProjectProvider(
        private val onProjectLoaded: (dynamic) -> Unit,
        private val onNewProjectAdded: (String, String, String) -> Unit,
        private val onFail: (String, ActionStatusMessage) -> Unit
) {
    fun loadProject(publicId: String, type: ProjectType, callback: (dynamic) -> Unit, onNotFound: () -> Unit) {
        if (type == ProjectType.EXAMPLE) {
            loadExample(publicId, callback);
        } else {
            loadProject(publicId, callback, onNotFound);
        }
    }

    fun deleteProject(id: String, type: ProjectType, callback: () -> Unit) {
        if (type == ProjectType.USER_PROJECT) {
            deleteProject(id, callback);
        } else if (type == ProjectType.PUBLIC_LINK) {
            callback();
        } else {
            throw Exception("Can't delete this project");
        }
    }

    fun renameProject(project: Project, newName: String) {
        blockContent();
        ajax(
                url = generateAjaxUrl("renameProject", json()),
                success = {
                    try {
                        project.name = newName;
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                type = RequestType.POST,
                dataType = DataType.TEXT,
                data = json("publicId" to project.publicId, "newName" to newName),
                timeout = 10000,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        unBlockContent();
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.rename_project_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    private fun loadExample(publicId: String, callback: (dynamic) -> Unit) {
        blockContent();
        ajax(
                url = generateAjaxUrl("loadExample", json()),
                success = { data ->
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                onProjectLoaded(data);
                                callback(data);
                            } else {
                                onFail(data, ActionStatusMessages.load_example_fail);
                            }
                        } else {
                            onFail("Incorrect data format.", ActionStatusMessages.load_example_fail);
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.JSON,
                type = RequestType.GET,
                timeout = 10000,
                data = json("publicId" to publicId),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_example_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        );
    }

    fun addNewProject(name: String) {
        blockContent();
        ajax(
                url = generateAjaxUrl("addProject", json()),
                success = { data ->
                    try {
                        onNewProjectAdded(name, data.projectId, data.fileId);
                        console.log("haha")
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                type = RequestType.POST,
                timeout = 10000,
                data = json("args" to name),
                dataType = DataType.JSON,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.add_project_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    private fun loadProject(publicId: String, callback: (dynamic) -> Unit, onNotFound: () -> Unit) {
        blockContent();
        ajax(
                url = generateAjaxUrl("loadProject", json()),
                success = { data ->
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                onProjectLoaded(data);
                                callback(data)
                            } else {
                                onFail(data, ActionStatusMessages.load_program_fail);
                            }
                        } else {
                            onFail("Incorrect data format.", ActionStatusMessages.load_program_fail);
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.JSON,
                data = json("publicId" to publicId),
                type = RequestType.GET,
                timeout = 10000,
                statusCode = json(
                        "404" to onNotFound
                ),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun forkProject(content: dynamic, callback: (dynamic) -> Unit, name: String) {
        blockContent();
        ajax(
                url = generateAjaxUrl("addProject", json()),
                success = { data ->
                    try {
                        callback(data);
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                type = RequestType.GET,
                timeout = 10000,
                dataType = DataType.JSON,
                data = json("content" to JSON.stringify(content), "args" to name),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun checkIfProjectExists(publicId: String, onExists: () -> Unit, onNotExists: () -> Unit) {
        ajax(
                url = generateAjaxUrl("checkIfProjectExists", json()),
                success = { data ->
                    if (data.exists == true) {
                        onExists()
                    } else {
                        onNotExists();
                    }
                },
                type = RequestType.POST,
                timeout = 10000,
                data = json("publicId" to publicId),
                dataType = DataType.JSON,
                error = { jqXHR, textStatus, errorThrown ->
                    onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                }
        )
    }


    private fun deleteProject(publicId: String, callback: () -> Unit) {
        blockContent();
        ajax(
                url = generateAjaxUrl("deleteProject", json()),
                success = {
                    try {
                        callback();
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = RequestType.POST,
                timeout = 10000,
                dataType = DataType.TEXT,
                data = json("publicId" to publicId),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.delete_program_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun saveProject(project: Project, publicId: String, callback: () -> Unit) {
        blockContent();
        ajax(
                url = generateAjaxUrl("saveProject", json()),
                success = {
                    try {
                        callback();
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                type = RequestType.POST,
                timeout = 10000,
                data = json("project" to JSON.stringify(project), "publicId" to publicId),
                dataType = DataType.TEXT,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                complete = ::unBlockContent
        )
    }
}
