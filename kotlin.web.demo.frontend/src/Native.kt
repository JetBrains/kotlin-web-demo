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
interface FileProvider {
    fun saveFile(file: File, callback: () -> Unit)
    fun renameFile(publicId: String, callback: (String) -> Unit, newName: String)
    fun deleteFile(file: File, callback: () -> Unit)

    fun loadOriginalFile(file: File, onSuccess: (dynamic) -> Unit, onNotFound: () -> Unit)
}

native
val fileProvider: FileProvider = noImpl

native
interface Project {
    fun getType(): ProjectType
    fun getName(): String
    fun getParent(): FolderView
    fun deleteFile(file: File)
    fun getConfiguration(): dynamic
    fun rename(newName: String)
    fun getPublicId(): String
}

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

native("$")
object JQuery{
    fun ajax(params: Json)
}
