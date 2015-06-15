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

import model.File
import model.Project
import model.ProjectType
import utils.blockContent
import utils.unBlockContent
import views.ActionStatusMessage
import kotlin.browser.localStorage

class ProjectProvider(
        private val onProjectLoaded: () -> Unit,
        private val onNewProjectAdded: (String, String, String) -> Unit,
        private val onFail: (String, ActionStatusMessage) -> Unit
) {

    fun loadProject(
            publicId: String,
            type: ProjectType,
            ignoreCache: Boolean = false,
            callback: (dynamic) -> Unit,
            onNotFound: () -> Unit
    ) {
        if (localStorage.getItem(publicId) != null && !ignoreCache) {
            val content: dynamic = JSON.parse(localStorage.getItem(publicId)!!)
            localStorage.removeItem(publicId)
            val files = arrayListOf<File>()
            for (fileId in content.files) {
                val fileContent: dynamic = JSON.parse(localStorage.getItem(fileId)!!)
                files.add(fileContent)
            }
            content.files = files
            callback(content)
            onProjectLoaded()
        } else {
            if (type == ProjectType.EXAMPLE) {
                loadExample(publicId, callback)
            } else {
                loadProject(publicId, callback, onNotFound)
            }
        }
    }

    fun deleteProject(id: String, type: ProjectType, callback: () -> Unit) {
        if (type == ProjectType.USER_PROJECT) {
            deleteProject(id, callback)
        } else if (type == ProjectType.PUBLIC_LINK) {
            callback()
        } else {
            throw Exception("Can't delete this project")
        }
    }

    fun renameProject(project: Project, newName: String) {
        blockContent()
        ajax(
                url = generateAjaxUrl("renameProject"),
                success = {
                    try {
                        project.name = newName
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTTPRequestType.POST,
                dataType = DataType.TEXT,
                data = json("publicId" to project.id, "newName" to newName),
                timeout = 10000,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        unBlockContent()
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.rename_project_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    private fun loadExample(publicId: String, callback: (dynamic) -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("loadExample"),
                success = { data ->
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                onProjectLoaded()
                                callback(data)
                            } else {
                                onFail(data, ActionStatusMessage.load_project_fail)
                            }
                        } else {
                            onFail("Incorrect data format.", ActionStatusMessage.load_project_fail)
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.JSON,
                type = HTTPRequestType.GET,
                timeout = 10000,
                data = json("publicId" to publicId),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.load_project_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun addNewProject(name: String) {
        blockContent()
        ajax(
                url = generateAjaxUrl("addProject"),
                success = { data ->
                    try {
                        onNewProjectAdded(name, data.projectId, data.fileId)
                        console.log("haha")
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTTPRequestType.POST,
                timeout = 10000,
                data = json("args" to name),
                dataType = DataType.JSON,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.add_project_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    private fun loadProject(publicId: String, callback: (dynamic) -> Unit, onNotFound: () -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("loadProject"),
                success = { data ->
                    try {
                        if (checkDataForNull(data)) {
                            if (checkDataForException(data)) {
                                onProjectLoaded()
                                callback(data)
                            } else {
                                onFail(data, ActionStatusMessage.load_project_fail)
                            }
                        } else {
                            onFail("Incorrect data format.", ActionStatusMessage.load_project_fail)
                        }
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.JSON,
                data = json("publicId" to publicId),
                type = HTTPRequestType.GET,
                timeout = 10000,
                statusCode = json(
                        "404" to onNotFound
                ),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.load_project_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun forkProject(content: Project, callback: (dynamic) -> Unit, name: String) {
        blockContent()
        ajax(
                url = generateAjaxUrl("addProject"),
                success = { data ->
                    try {
                        callback(data)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTTPRequestType.GET,
                timeout = 10000,
                dataType = DataType.JSON,
                data = json("content" to JSON.stringify(content), "args" to name),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.save_program_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun checkIfProjectExists(publicId: String, onExists: () -> Unit, onNotExists: () -> Unit) {
        ajax(
                url = generateAjaxUrl("checkIfProjectExists"),
                success = { data ->
                    if (data.exists == true) {
                        onExists()
                    } else {
                        onNotExists()
                    }
                },
                type = HTTPRequestType.POST,
                timeout = 10000,
                data = json("publicId" to publicId),
                dataType = DataType.JSON,
                error = { jqXHR, textStatus, errorThrown ->
                    onFail(textStatus + " : " + errorThrown, ActionStatusMessage.save_program_fail)
                }
        )
    }


    private fun deleteProject(publicId: String, callback: () -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("deleteProject"),
                success = {
                    try {
                        callback()
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTTPRequestType.POST,
                timeout = 10000,
                dataType = DataType.TEXT,
                data = json("publicId" to publicId),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.delete_program_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun saveProject(project: Project, publicId: String, callback: () -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("saveProject"),
                success = {
                    try {
                        callback()
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTTPRequestType.POST,
                timeout = 10000,
                data = json("project" to JSON.stringify(project), "publicId" to publicId),
                dataType = DataType.TEXT,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.save_program_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }
}
