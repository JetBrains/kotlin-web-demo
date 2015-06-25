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

import html4k.div
import html4k.dom.create
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLPreElement
import utils.Listenable
import utils.VarListener
import utils.jquery.JQuery
import utils.jquery.find
import utils.jquery.toArray
import java.util.ArrayList
import kotlin.browser.document


public class Task(
        id: String,
        name: String,
        completed: Boolean,
        parent: Folder,
        onFileDeleted: (String) -> Unit,
        onContentLoaded: (ArrayList<File>) -> Unit,
        onContentNotFound: () -> Unit
) : Project(ProjectType.TASK, id, name, parent, onFileDeleted, onContentLoaded, onContentNotFound) {
    val completedListener =  VarListener<Boolean>()
    var completed by Listenable(completed, completedListener)

    var taskWindows = emptyList<TaskWindow>()

    var help: HTMLElement = document.create.div("taskHelp")

    override fun contentLoaded(content: dynamic) {
        val helpContent = JQuery.parseHTML(content.help)
        helpContent?.forEach { help.appendChild(it) }
        if (content.taskWindows != null) this.taskWindows = (content.taskWindows as Array<TaskWindow>).toArrayList();
        super.contentLoaded(content);
    }
}

native interface TaskWindow {
    val line: Int;
    val start: Int;
    val length: Int;
}