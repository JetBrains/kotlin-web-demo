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

import jquery.JQuery
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import providers.FileProvider
import providers.HeadersProvider
import providers.ProjectProvider
import views.FolderView

/**
 * Created by Semyon.Atamas on 4/3/2015.
 */

native
fun JQuery.hide()

native
fun JQuery.show()

native
fun JQuery.children(): JQuery

native
fun JQuery.unbind(s: String)

native
fun JQuery.on(s: String, onClose: (Event) -> Unit)

native
interface Error {
    val className: String
    val interval: dynamic
    val message: String
    val severity: String
}

native
enum class FileType {
    KOTLIN_FILE,
    KOTLIN_TEST_FILE,
    JAVA_FILE
}

native
enum class ProjectType {
    EXAMPLE,
    USER_PROJECT,
    PUBLIC_LINK
}

native
val fileProvider: FileProvider = noImpl

native
val headersProvider: HeadersProvider = noImpl

//native("ProjectData")
//class Project(type: ProjectType, publicId: String, name: String, parent: FolderView) {
//    var onFileDeleted: (String)-> Unit
//    var onFileAdded: (model.model.File)-> Unit
//    var onContentNotFound: () -> Unit
//    var onContentLoaded: () -> Unit
//    var onRenamed: (String) -> Unit
//    var onNotRevertible: () -> Unit
//    var onModified: (Boolean) -> Unit
//    fun getType(): ProjectType
//    fun getName(): String
//    fun getParent(): FolderView
//    fun deleteFile(file: model.model.File)
//    fun getConfiguration(): dynamic
//    fun rename(newName: String)
//    fun getPublicId(): String
//    fun addEmptyFile(filename: String, publicId: String)
//    fun loadOriginal()
//    fun getFiles(): Array<model.model.File>
//    fun makeNotRevertible()
//    fun isContentLoaded(): Boolean
//    fun isEmpty(): Boolean
//    fun loadContent(fromServer: Boolean)
//}

native
val projectProvider: ProjectProvider = noImpl

native
interface LoginModel{
    fun login(type: String)
    fun logout()
    fun getUserName()
}

native
fun decodeURI(uri:String): String

native
var CodeMirror: dynamic = noImpl

native
fun removeKotlinExtension(name: String): String

native
fun addKotlinExtension(name: String): String

native
fun generateAjaxUrl(type: String, parameters: Json): String

native
fun checkDataForNull(data: dynamic): Boolean

native
fun checkDataForException(data: dynamic): Boolean

native
val statusBarView: dynamic