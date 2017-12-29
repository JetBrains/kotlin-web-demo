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

package model

import application.Application
import utils.Listenable
import utils.VarListener
import kotlin.js.Json
import kotlin.js.json

abstract class Project(
        val type: ProjectType,
        val id: String,
        name: String,
        val parent: Folder,
        private val onFileDeleted: (String) -> Unit,
        private val onContentLoaded: (ArrayList<File>) -> Unit,
        private val onContentNotFound: () -> Unit
) {
    fun toJSON(): Json {
        return json(
                "id" to id,
                "name" to name,
                "args" to args,
                "compilerVersion" to compilerVersion,
                "confType" to confType,
                "originUrl" to originUrl,
                "files" to files.filter { it.isModifiable },
                "readOnlyFileNames" to files.filter { !it.isModifiable }.map { it.name }
        )
    }

    fun save() {
        Application.projectProvider.saveProject(this, { onModified() })
    }

    public open fun loadOriginal() {
        loadContent(true)
    }

    fun loadContent(ignoreCache: Boolean) {
        Application.projectProvider.loadProject(
                id,
                type,
                ignoreCache,
                { content ->
                    val files = arrayListOf<File>()
                    for (fileContent in content.files) {
                        val file = File.fromJSON(this, fileContent)
                        file.listenableIsModified.addModifyListener { onModified() }
                        files.add(file)
                    }
                    content.files = files
                    contentLoaded(content)
                },
                onContentNotFound
        )
    }

    fun onModified() {
        modified = isModified()
    }

    fun deleteFile (file: File) {
        files.remove(file)
        onFileDeleted(file.id)
    }

    open fun contentLoaded(content: dynamic) {
        contentLoaded = true
        originUrl = content.originUrl
        args = content.args
        compilerVersion = content.compilerVersion
        confType = content.confType
        files = content.files
        revertible = if (content.hasOwnProperty("revertible")) content.revertible else true
        onContentLoaded(files)
        onModified()
    }

    fun setContent(content: dynamic) {
        if (!contentLoaded) {
            val files = arrayListOf<File>()
            for (fileContent in content.files) {
                val file = File.fromJSON(this, fileContent)
                files.add(file)
                file.listenableIsModified.addModifyListener { onModified() }
            }
            content.files = files
            contentLoaded(content)
            contentLoaded = true
        } else {
            throw Exception("Content was already loaded")
        }
    }

    fun setDefaultContent() {
        if (!contentLoaded) {
            contentLoaded = true
        } else {
            throw Exception("Content was already loaded")
        }
    }

    private fun isModified() = files.any { it.isModified }

    var files = arrayListOf<File>()
    open val name = name
    var contentLoaded = false
    var compilerVersion: String? = null
    var args = ""
    var confType = "java"
    var originUrl = null
    val modifiedListener = VarListener<Boolean>()
    var modified by Listenable(false, modifiedListener)
    val revertibleListener = VarListener<Boolean>()
    var revertible by Listenable(true, revertibleListener)
}


enum class ProjectType {
    EXAMPLE,
    TASK,
    USER_PROJECT,
    PUBLIC_LINK;

    fun toJSON(): String{
        return name
    }
}