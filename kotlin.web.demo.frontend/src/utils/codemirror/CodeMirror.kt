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
import kotlin.js.Json

external class CodeMirror(element: HTMLElement, parameters: Json) {
    companion object CodeMirror {
        val hint: dynamic = definedExternally
        val commands: dynamic = definedExternally
        fun runMode(text: String, mode: String, outputElement: HTMLElement): Unit = definedExternally
        fun fromTextArea(textArea: HTMLTextAreaElement, json: Json): utils.codemirror.CodeMirror = definedExternally
        fun registerHelper(type: String, name: String, value: Any): Unit = definedExternally
        fun on(obj: Any, action: String, callback: () -> Unit): Unit = definedExternally
        fun colorize(elements: HTMLCollection): Unit = definedExternally
    }

    class Doc(text: String, mode: String = definedExternally, firstLineNumber: Int = definedExternally) {
        fun markText(start: Position, end: Position, json: Json): Any = definedExternally
        fun getEditor(): utils.codemirror.CodeMirror = definedExternally
        fun addLineWidget(lineNo: Int, help: HTMLElement?, options: Json): LineWidget = definedExternally
        fun setSelection(anchor: Position, head: Position): Unit = definedExternally
    }

    class LineWidget {
        val node: HTMLElement = definedExternally
        val line: Line = definedExternally
        fun clear(): Unit = definedExternally
        fun changed(): Unit = definedExternally
    }

    class Line {
        val widgets: Array<LineWidget>? = definedExternally
        fun lineNo(): Int = definedExternally
    }

    class TextMarker {
        val className: String? = definedExternally
        fun find(): Range = definedExternally
        fun clear(): Unit = definedExternally
    }

    fun getCursor(): Position = definedExternally
    fun getDoc(): Doc = definedExternally
    fun getTokenAt(pos: Position): Token = definedExternally
    fun replaceRange(replacement: String, from: Position, to: Position = definedExternally, origin: String? = definedExternally): Unit = definedExternally
    fun execCommand(s: String): Unit = definedExternally
    fun on(action: String, callback: (utils.codemirror.CodeMirror) -> Unit): Unit = definedExternally
    fun <T> on(action: String, callback: (utils.codemirror.CodeMirror, additionalInfo: T) -> Unit): Unit = definedExternally
    fun getValue(): String = definedExternally
    fun setOption(name: String, value: Any): Unit = definedExternally
    fun refresh(): Unit = definedExternally
    fun setCursor(lineNo: Int, charNo: Int): Unit = definedExternally
    fun focus(): Unit = definedExternally
    fun setValue(text: String): Unit = definedExternally
    fun setHistory(history: dynamic): Unit = definedExternally
    fun getHistory(): dynamic = definedExternally
    fun clearHistory(): Unit = definedExternally
    fun cursorCoords(): dynamic = definedExternally
    fun clearGutter(gutter: String): Unit = definedExternally
    fun markText(start: Position, end: Position, json: Json): Any = definedExternally
    fun setGutterMarker(line: Int, gutter: String, element: HTMLElement): Unit = definedExternally
    fun lineInfo(line: Int): dynamic = definedExternally
    fun lineCount(): Int = definedExternally
    fun indentLine(lineNo: Int): Unit = definedExternally
    fun operation(function: () -> Unit): Unit = definedExternally
    fun swapDoc(document: Doc): Unit = definedExternally
    fun openDialog(template: HTMLElement, callback: () -> Unit, options: dynamic): (() -> Unit) = definedExternally
    fun addLineWidget(lineNo: Int, help: HTMLElement?, options: Json): Unit = definedExternally
    fun setSelection(anchor: Position, head: Position): Unit = definedExternally
    fun getLineHandle(i: Int): Line = definedExternally
    fun coordsChar(cursorCoordinates: Coordinates): Position = definedExternally
    fun findMarksAt(position: Position): Array<TextMarker> = definedExternally
    fun listSelections(): Array<Selection> = definedExternally
}

data class Position(val line: Int, val ch: Int)

data class Range(val from: Position, val to: Position)

data class Selection(val anchor: Position, val head: Position)

data class Coordinates(val left: Double, val top: Double)

external interface Token {
    val start: Int
    val end: Int
    val string: String
    val type: String?
    val state: dynamic
}

data class Hint(val from: Position, val to: Position, var list: Array<CompletionView>)


external interface CompletionView {
    val text: String
    val displayText: String
    fun render(element: HTMLElement, self: dynamic, data: dynamic);
    fun hint(cm: CodeMirror, self: dynamic, data: dynamic)
}