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

import model.Folder
import model.LevelInfo
import model.ProjectType
import utils.blockContent
import utils.unBlockContent
import views.ActionStatusMessage
import views.tree.ProjectHeader
import views.tree.TaskHeader
import kotlin.browser.localStorage

class HeadersProvider(
        private val onFail: (String, ActionStatusMessage) -> Unit,
        private val onHeadersLoaded: () -> Unit,
        private val onProjectHeaderLoaded: () -> Unit,
        private val onProjectHeaderNotFound: () -> Unit
) {
    fun createFolder(content: FolderContent, type: ProjectType): Folder {
        val projects = content.projects.map {
            val modified = it.modified || localStorage.getItem(it.publicId) != null
            val completedProjects = JSON.parse<Array<String>>(localStorage["completedProjects"] ?: "[]")
            val completed = (it.completed ?: false) || (it.publicId in completedProjects)
            if(type == ProjectType.TASK){
                TaskHeader(it.name, it.publicId, type, modified, completed)
            } else {
                ProjectHeader(it.name, it.publicId, type, modified)
            }
        }
        val childFolders = content.childFolders.map {  createFolder(it, type) }
        val levels = content.levels?.asList() ?: emptyList<LevelInfo>()
        val folder = Folder(content.name, content.id, projects, childFolders, content.isTaskFolder, levels)
        return folder
    }

    fun createPublicLinksFolder(userProjectIds: List<String>): Folder {
        val publicLinks = if (localStorage.getItem("publicLinks") != null) {
            val parsedArray = JSON.parse<Array<ProjectInfo>>(localStorage.getItem("publicLinks")!!)
            parsedArray
                    .map {
                        val modified = localStorage.getItem(it.publicId) != null
                        ProjectHeader(it.name, it.publicId, ProjectType.PUBLIC_LINK, modified)
                    }
                    .filter { it.publicId !in userProjectIds }
        } else {
            emptyList()
        }

        return Folder(
                "Public links",
                "PublicLinks",
                publicLinks,
                emptyList(),
                false
        )
    }

    fun getAllHeaders(callback: (List<Folder>) -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl("loadHeaders"),
                success = { foldersContent: Array<FolderContent> ->
                    try {
                        val folders = arrayListOf<Folder>()
                        foldersContent.mapTo(folders, {
                            val type = when {
                                it.name == "My programs" -> ProjectType.USER_PROJECT
                                it.name == "Advent of Code" -> ProjectType.ADVENT_OF_CODE_PROJECT
                                it.isTaskFolder -> ProjectType.TASK
                                else  -> ProjectType.EXAMPLE
                            }
                            createFolder(it, type)
                        })
                        val myProgramsFolder = folders.first { it.name == "My programs" }
                        folders.add(createPublicLinksFolder(myProgramsFolder.projects.map { it.publicId }))
                        callback(folders)
                        onHeadersLoaded()
                    } finally {
                        unBlockContent()
                    }
                },
                dataType = DataType.JSON,
                type = HTTPRequestType.GET,
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

    fun getProjectHeaderById(id: String, callback: (ProjectHeader) -> Unit) {
        blockContent()
        ajax(
                url = generateAjaxUrl(REQUEST_TYPE.LOAD_PROJECT_NAME),
                success = { name ->
                    try {
                        callback(ProjectHeader(name, id, ProjectType.PUBLIC_LINK, false))
                        onProjectHeaderLoaded()
                    } catch (e: Throwable) {
                        console.log(e)
                    }
                },
                type = HTTPRequestType.GET,
                timeout = 10000,
                dataType = DataType.TEXT,
                data = json(
                        "project_id" to id
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

@native
interface FolderContent {
    val name: String
    val id: String
    val projects: Array<ProjectInfo>
    val childFolders: Array<FolderContent>
    val isTaskFolder: Boolean
    val levels: Array<LevelInfo>?
}

@native
interface ProjectInfo {
    val name: String
    val publicId: String
    val modified: Boolean
    val completed: Boolean?
}

