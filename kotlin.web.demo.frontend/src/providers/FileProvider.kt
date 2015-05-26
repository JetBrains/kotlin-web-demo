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

import File
import Project
import ProjectType
import addKotlinExtension
import generateAjaxUrl
import utils.*

class FileProvider(
        private val onFail: (String, String) -> Unit,
        private val onOriginalFileLoaded: (dynamic) -> Unit
) {
    fun checkFileExistence(publicId: String, onNotExists: () -> Unit) {
        ajax(
                url = generateAjaxUrl("checkFileExistence", json()),
                type = RequestType.POST,
                timeout = 10000,
                data = json("publicId" to publicId),
                dataType = DataType.JSON,
                success = { data ->
                    if (!data.exists) {
                        onNotExists();
                    }
                },
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

    fun loadOriginalFile(file: File, onSuccess: (dynamic) -> Unit, onNotFound: () -> Unit) {
        when (file.project.getType()) {
            ProjectType.EXAMPLE -> {
                blockContent();
                ajax(
                        url = generateAjaxUrl("loadExampleFile", json()),
                        success = { data ->
                            try {
                                unBlockContent();
                                onSuccess(data);
                                onOriginalFileLoaded(data);
                            } catch (e: Throwable) {
                                console.log(e);
                            }
                        },
                        type = RequestType.POST,
                        timeout = 10000,
                        data = json("publicId" to file.id),
                        dataType = DataType.JSON,
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
            ProjectType.PUBLIC_LINK -> {
                blockContent();
                ajax(
                        url = generateAjaxUrl("loadProjectFile", json()),
                        success = { data ->
                            try {
                                unBlockContent();
                                onSuccess(data);
                                onOriginalFileLoaded(data);
                            } catch (e: Throwable) {
                                console.log(e);
                            }
                        },
                        type = RequestType.POST,
                        timeout = 10000,
                        data = json("publicId" to file.id),
                        dataType = DataType.JSON,
                        statusCode = json(
                                "404" to onNotFound
                        ),
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
            else -> {
                throw Exception("User files can't be reloaded from server.");
            }
        }
    }

    fun addNewFile(project: Project, filename: String) {
        blockContent();
        val filenameWithExtension = addKotlinExtension(filename);
        ajax(
                url = generateAjaxUrl("addFile", json()),
                success = { publicId ->
                    try {
                        project.addEmptyFile(filenameWithExtension, publicId);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.TEXT,
                type = RequestType.POST,
                timeout = 10000,
                data = json("publicId" to project.getPublicId(), "filename" to filenameWithExtension),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.save_program_fail);
                    } catch (e: Throwable) {
                        unBlockContent();
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun renameFile(publicId: String, callback: (String) -> Unit, newName: String) {
        blockContent();
        ajax(
                url = generateAjaxUrl("renameFile", json()),
                success = {
                    try {
                        callback(newName);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                dataType = DataType.TEXT,
                type = RequestType.POST,
                timeout = 10000,
                data = json(
                        "publicId" to publicId,
                        "newName" to newName
                ),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus, errorThrown);
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                complete = ::unBlockContent
        )
    }

    fun deleteFile(file: File, callback: () -> Unit) {
        var data = if (file.isModifiable) {
            json(
                    "fileId" to file.id,
                    "modifiable" to true
            )
        } else {
            json(
                    "fileName" to file.name,
                    "modifiable" to false,
                    "projectId" to file.project.getPublicId()
            )
        }
        blockContent();
        ajax(
                url = generateAjaxUrl("deleteFile", json()),
                success = {
                    try {
                        callback();
                    } catch (e: Throwable) {
                        console.log(e);
                    }
                },
                type = RequestType.POST,
                data = data,
                dataType = DataType.TEXT,
                timeout = 10000,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessages.load_program_fail);
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        );
    }

    public fun saveFile(file: File, callback: () -> Unit) {
        blockContent();
        ajax(
                url = generateAjaxUrl("saveFile", json()),
                type = RequestType.POST,
                timeout = 10000,
                dataType = DataType.TEXT,
                success = {
                    try {
                        callback();
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                data = json("file" to JSON.stringify(file)),
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
}

