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

import org.w3c.dom.Element
import views.FolderView

/**
 * Created by Semyon.Atamas on 4/3/2015.
 */

native("$")
fun jq(element: Element): dynamic

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
}

native
val fileProvider: FileProvider = noImpl

native
interface Project {
    fun getType(): ProjectType
    fun getName(): String
    fun getParent(): FolderView
}

native
val projectProvider: dynamic = noImpl