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
import views.editor.Error
import utils.Listenable
import utils.VarListener
import kotlin.browser.localStorage

class File(
        val project: Project,
        name: String,
        val id: String,
        text: String = "",
        var originalText: String = text,
        val isModifiable: Boolean = true,
        val type: String = FileType.KOTLIN_FILE.name(),
        isRevertible: Boolean = true
) {
    val listenableName = VarListener<String>();
    var name: String by Listenable(name, listenableName)

    val listenableIsModified = VarListener<Boolean>()
    var isModified: Boolean by Listenable(false, listenableIsModified)

    val listenableIsRevertible = VarListener<Boolean>()
    var isRevertible: Boolean by Listenable(isRevertible, listenableIsRevertible)

    var text: String = text
        set(newText: String) {
            isModified = newText != originalText
            $text = newText
        }

    fun toJSON(): dynamic {
        val result = js("({})")
        result.name = name
        result.text = text
        result.publicId = id
        return result
    }

    companion object {

        fun fromJSON(project: dynamic, obj: dynamic) =
                File(project, obj.name, obj.publicId, obj.text, obj.originalText, obj.modifiable, obj.type, obj.revertible)
    }

    //TODO following functions from file to some other class
    fun save() {
        if (project.type == ProjectType.USER_PROJECT && isModifiable) {
           Application.fileProvider.saveFile(this, {
                originalText = text
                isModified = text != originalText
            });
        }
    }

    fun dumpToLocalStorage() {
        if (project.type != ProjectType.USER_PROJECT) {
            //TODO don't save editor info
            val result = js("({})")
            result.name = name
            result.originalText = originalText
            result.text = text
            result.publicId = id
            result.modifiable = isModifiable
            result.type = type
            result.revertible = isRevertible
            localStorage.set(id, JSON.stringify(result));
        }
    }

    fun loadOriginal(){

    }
}

enum class FileType {
    KOTLIN_FILE,
    KOTLIN_TEST_FILE,
    JAVA_FILE
}