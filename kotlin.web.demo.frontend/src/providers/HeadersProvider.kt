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

import model.ProjectType
import utils.blockContent
import utils.unBlockContent
import views.ActionStatusMessage
import views.tree.ProjectHeader
import kotlin.browser.localStorage

class HeadersProvider(
        private val onFail: (String, ActionStatusMessage) -> Unit,
        private val onHeadersLoaded: () -> Unit,
        private val onProjectHeaderLoaded: () -> Unit,
        private val onProjectHeaderNotFound: () -> Unit
) {
    fun addHeaderInfo(folder: dynamic) {
        folder.projects.forEach { project: dynamic ->
            project.type = if (folder.name == "My programs") ProjectType.USER_PROJECT else ProjectType.EXAMPLE
            Unit
        }
        folder.childFolders.forEach({folder -> addHeaderInfo(folder)})
    }

    fun getAllHeaders(callback: (dynamic) -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("loadHeaders"),
                success = { folders ->
                    try {
                        if (checkDataForNull(folders)) {
                            if (checkDataForException(folders)) {
                                folders.forEach({folder -> addHeaderInfo(folder)})

                                var publicLinks = if (localStorage.getItem("publicLinks") != null) {
                                    val parsedArray = JSON.parse<Array<dynamic>>(localStorage.getItem("publicLinks")!!)
                                    parsedArray.map { ProjectHeader(it.name, it.publicId, ProjectType.PUBLIC_LINK) }
                                } else {
                                    emptyList()
                                }

                                //TODO remove user project public links

                                folders.push(json(
                                        "name" to "Public links",
                                        "id" to "PublicLinks",
                                        "childFolders" to arrayOf<dynamic>(),
                                        "projects" to publicLinks
                                ))

                                callback(folders)
                                onHeadersLoaded()
                            } else {
                                onFail(folders, ActionStatusMessage.load_headers_fail)
                            }
                        } else {
                            onFail("Incorrect data format.", ActionStatusMessage.load_headers_fail)
                        }
                    } finally {
                        unBlockContent()
                    }
                },
                dataType = DataType.JSON,
                type = HTMLRequestType.GET,
                timeout = 10000,
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.load_headers_fail)
                    } finally {
                        unBlockContent()
                    }
                }
        )
    }

    fun getHeaderByFilePublicId(publicId: String, project_id: String, callback: (ProjectHeader) -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("loadProjectInfoByFileId"),
                success = { data ->
                    try {
                        callback(ProjectHeader(data.name, data.publicId, ProjectType.PUBLIC_LINK))
                        onProjectHeaderLoaded()
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTMLRequestType.GET,
                timeout = 10000,
                dataType = DataType.JSON,
                data = json(
                        "publicId" to publicId,
                        "project_id" to project_id
                ),
                statusCode = json(
                        "404" to onProjectHeaderNotFound
                ),
                error = { jqXHR, textStatus, errorThrown ->
                    try {
                        onFail(textStatus + " : " + errorThrown, ActionStatusMessage.get_header_fail)
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                complete = ::unBlockContent
        )
    }
}
