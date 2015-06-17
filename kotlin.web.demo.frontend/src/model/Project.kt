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
import utils.addKotlinExtension
import java.util.ArrayList
import kotlin.browser.localStorage

class Project(
        val type: ProjectType,
        val id: String,
        name: String,
        val parent: Folder,
        private val onFileAdded: (File) -> Unit,
        private val onFileDeleted: (String) -> Unit,
        private val onContentLoaded: (ArrayList<File>) -> Unit,
        private val onContentNotFound: () -> Unit
) {
    fun toJSON(): Json {
        return json(
                "id" to id,
                "name" to name,
                "args" to args,
                "confType" to confType,
                "originUrl" to originUrl,
                "files" to files.filter { it.isModifiable },
                "readOnlyFileNames" to files.filter { !it.isModifiable }.map { it.name }
        )
    }

    fun save() {
        when (type) {
            ProjectType.USER_PROJECT -> Application.projectProvider.saveProject(this, id, { onModified() })
            else -> {
                if (isModified()) {
                    var fileIDs = arrayListOf<String>()
                    for (file in files) {
                        Application.fileProvider.saveFile(file)
                        fileIDs.add(file.id)
                    }
                    localStorage.setItem(id, JSON.stringify(json(
                            "name" to name,
                            "files" to fileIDs.toTypedArray(),
                            "args" to args,
                            "confType" to confType,
                            "originUrl" to originUrl,
                            "type" to type,
                            "publicId" to id,
                            "revertible" to revertible
                    )))
                    files.forEach { Application.fileProvider.saveFile(it) }
                } else {
                    localStorage.removeItem(id)
                }
            }
        }
    }

    fun loadOriginal() {
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

    fun addEmptyFile(name: String, publicId: String): File {
        var file = File(this, name, publicId)
        file.listenableIsModified.addModifyListener {onModified()}
        files.add(file)
        onFileAdded(file)
        return file
    }

    fun addFileWithMain(name: String, publicId: String): File {
        var file = File(
                this,
                addKotlinExtension(name),
                publicId,
                "fun main(args: Array<String>) {\n\n}"
        )
        file.listenableIsModified.addModifyListener {onModified()}
        files.add(file)
        onFileAdded(file)
        return file
    }

    fun deleteFile (file: File) {
        files.remove(file)
        onFileDeleted(file.id)
    }

    private fun contentLoaded(content: dynamic) {
        contentLoaded = true
        originUrl = content.originUrl
        args = content.args
        name = content.name
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
    val nameListener = VarListener<String>()
    var name by Listenable(name, nameListener)
    var contentLoaded = false
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
    USER_PROJECT,
    PUBLIC_LINK;

    fun toJSON(): String{
        return name()
    }
}