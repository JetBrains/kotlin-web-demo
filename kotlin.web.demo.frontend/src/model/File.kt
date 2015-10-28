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
import kotlin.browser.localStorage

class File(
        val project: Project,
        name: String,
        val id: String,
        var text: String = "",
        userText: String? = text,
        val isModifiable: Boolean = true,
        val type: String = FileType.KOTLIN_FILE.name(),
        isRevertible: Boolean = true,
        val taskWindows: List<TaskWindow> = emptyList(),
        val solutions: Array<String>? = null
) {
    var userText: String = userText ?: text
        set(newText: String) {
            isModified = newText != text
            field = newText
        }
    val listenableName = VarListener<String>()

    var name: String by Listenable(name, listenableName)
    val listenableIsModified = VarListener<Boolean>()

    var isModified: Boolean by Listenable(text != this.userText, listenableIsModified)
    val listenableIsRevertible = VarListener<Boolean>()

    var isRevertible: Boolean by Listenable(isRevertible, listenableIsRevertible)

    fun toJSON(): dynamic {
        val result = js("({})")
        result.name = name
        result.text = userText
        result.publicId = id
        return result
    }

    companion object {
        fun fromJSON(project: dynamic, obj: dynamic) =
                File(
                        project,
                        obj.name,
                        obj.publicId,
                        obj.text,
                        obj.userText,
                        obj.modifiable,
                        obj.type,
                        obj.revertible,
                        (obj.taskWindows as Array<TaskWindow>?)?.toArrayList() ?: emptyList(),
                        obj.solutions
                )
    }

}

enum class FileType {
    KOTLIN_FILE,
    KOTLIN_TEST_FILE,
    JAVA_FILE
}