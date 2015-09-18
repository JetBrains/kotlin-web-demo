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

package views.tree

import model.Project
import model.ProjectType
import model.Task
import kotlin.dom.addClass
import kotlin.dom.removeClass


class TaskView(
        header: TaskHeader,
        parent: FolderView,
        onHeaderClick: (ProjectView) -> Unit,
        onSelected: (ProjectView) -> Unit,
        private val onCompleted: (TaskView) -> Unit,
        private val onReverted: (TaskView) -> Unit
) : ProjectView(header, parent, onHeaderClick, onSelected) {

    init {
        if (header.completed) headerElement.addClass("completed")
    }

    override fun initProject(header: ProjectHeader): Project {
        if (header !is TaskHeader) throw Exception("Wrong header type.")
        val task = Task(
                header.publicId,
                header.name,
                header.completed,
                parent.content,
                onFileDeleted,
                onContentLoaded,
                onContentNotFound
        )
        task.completedListener.addModifyListener { event ->
            if (event.newValue) {
                headerElement.addClass("completed")
                onCompleted(this)
            } else {
                headerElement.removeClass("completed")
                onReverted(this)
            }
        }
        task.modifiedListener.addModifyListener { event ->
            if (event.newValue) {
                headerElement.addClass("modified")
            } else {
                headerElement.removeClass("modified")
            }
        }
        return task
    }

}

class TaskHeader(name: String, publicId: String, type: ProjectType, modified: Boolean, val completed: Boolean) : ProjectHeader(name, publicId, type, modified)