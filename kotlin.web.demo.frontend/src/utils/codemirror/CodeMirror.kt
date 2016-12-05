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

package utils.codemirror

import org.w3c.dom.HTMLCollection
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event

@native
class CodeMirror(element: HTMLElement, parameters: Json) {
    companion object CodeMirror {
        val hint: dynamic = noImpl
        val commands: dynamic = noImpl
        fun runMode(text: String, mode: String, outputElement: HTMLElement): Unit = noImpl
        fun fromTextArea(textArea: HTMLTextAreaElement, json: Json): utils.codemirror.CodeMirror = noImpl
        fun registerHelper(type: String, name: String, value: Any): Unit = noImpl
        fun on(obj: Any, action: String, callback: () -> Unit): Unit = noImpl
        fun colorize(elements: HTMLCollection): Unit = noImpl
    }

    class Doc(text: String, mode: String = "", firstLineNumber: Int = 1) {
        fun markText(start: Position, end: Position, json: Json): Any = noImpl
        fun getEditor(): utils.codemirror.CodeMirror = noImpl
        fun addLineWidget(lineNo: Int, help: HTMLElement?, options: Json): LineWidget = noImpl
        fun setSelection(anchor: Position, head: Position): Unit = noImpl
    }

    class LineWidget {
        val node: HTMLElement = noImpl
        val line: Line = noImpl
        fun clear(): Unit = noImpl
        fun changed(): Unit = noImpl
    }

    class Line {
        val widgets: Array<LineWidget>? = noImpl
        fun lineNo(): Int = noImpl
    }

    class TextMarker {
        val className: String? = noImpl
        fun find(): Range = noImpl
        fun clear(): Unit = noImpl
    }

    fun getCursor(): Position = noImpl
    fun getDoc(): Doc = noImpl
    fun getTokenAt(pos: Position): Token = noImpl
    fun replaceRange(replacement: String, from: Position, to: Position = from, origin: String? = null): Unit = noImpl
    fun execCommand(s: String): Unit = noImpl
    fun on(action: String, callback: (utils.codemirror.CodeMirror) -> Unit): Unit = noImpl
    fun <T> on(action: String, callback: (utils.codemirror.CodeMirror, additionalInfo: T) -> Unit): Unit = noImpl
    fun getValue(): String = noImpl
    fun setOption(name: String, value: Any): Unit = noImpl
    fun refresh(): Unit = noImpl
    fun setCursor(lineNo: Int, charNo: Int): Unit = noImpl
    fun focus(): Unit = noImpl
    fun setValue(text: String): Unit = noImpl
    fun setHistory(history: dynamic): Unit = noImpl
    fun getHistory(): dynamic = noImpl
    fun clearHistory(): Unit = noImpl
    fun cursorCoords(): dynamic = noImpl
    fun clearGutter(gutter: String): Unit = noImpl
    fun markText(start: Position, end: Position, json: Json): Any = noImpl
    fun setGutterMarker(line: Int, gutter: String, element: HTMLElement): Unit = noImpl
    fun lineInfo(line: Int): dynamic = noImpl
    fun lineCount(): Int = noImpl
    fun indentLine(lineNo: Int): Unit = noImpl
    fun operation(function: () -> Unit): Unit = noImpl
    fun swapDoc(document: Doc): Unit = noImpl
    fun openDialog(template: HTMLElement, callback: () -> Unit, options: dynamic): (() -> Unit) = noImpl
    fun addLineWidget(lineNo: Int, help: HTMLElement?, options: Json): Unit = noImpl
    fun setSelection(anchor: Position, head: Position): Unit = noImpl
    fun getLineHandle(i: Int): Line = noImpl
    fun coordsChar(cursorCoordinates: Coordinates): Position = noImpl
    fun findMarksAt(position: Position): Array<TextMarker> = noImpl
    fun listSelections(): Array<Selection> = noImpl
}

data class Position(val line: Int, val ch: Int)

data class Range(val from: Position, val to: Position)

data class Selection(val anchor: Position, val head: Position)

data class Coordinates(val left: Double, val top: Double)

@native interface Token {
    val start: Int
    val end: Int
    val string: String
    val type: String?
    val state: dynamic
}

data class Hint(val from: Position, val to: Position, var list: Array<CompletionView>)


@native interface CompletionView {
    val text: String
    val displayText: String
    fun render(element: HTMLElement, self: dynamic, data: dynamic);
    fun hint(cm: CodeMirror, self: dynamic, data: dynamic)
}