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
        val hint: dynamic
        val commands: dynamic
        fun runMode(text: String, mode: String, outputElement: HTMLElement)
        fun fromTextArea(textArea: HTMLTextAreaElement, json: Json): utils.codemirror.CodeMirror
        fun registerHelper(type: String, name: String, value: Any)
        fun on(obj: Any, action: String, callback: () -> Unit)
        fun colorize(elements: HTMLCollection)
    }

    class Doc(text: String, mode: String = "", firstLineNumber: Int = 1) {
        fun markText(start: Position, end: Position, json: Json): Any
        fun getEditor(): utils.codemirror.CodeMirror
        fun addLineWidget(lineNo: Int, help: HTMLElement?, options: Json): LineWidget
        fun setSelection(anchor: Position, head: Position)
    }

    class LineWidget {
        val node: HTMLElement
        val line: Line
        fun clear()
        fun changed()
    }

    class Line {
        val widgets: Array<LineWidget>?
        fun lineNo(): Int
    }

    class TextMarker {
        val className: String?
        fun find(): Range
        fun clear()
    }

    fun getCursor(): Position = noImpl
    fun getDoc(): Doc = noImpl
    fun getTokenAt(pos: Position): Token = noImpl
    fun replaceRange(replacement: String, from: Position, to: Position = from, origin: String? = null): Nothing = noImpl
    fun execCommand(s: String): Nothing = noImpl
    fun on(action: String, callback: (utils.codemirror.CodeMirror) -> Unit): Nothing = noImpl
    fun <T> on(action: String, callback: (utils.codemirror.CodeMirror, additionalInfo: T) -> Unit): Nothing = noImpl
    fun getValue(): String = noImpl
    fun setOption(name: String, value: Any): Nothing = noImpl
    fun refresh(): Nothing = noImpl
    fun setCursor(lineNo: Int, charNo: Int): Nothing = noImpl
    fun focus(): Nothing = noImpl
    fun setValue(text: String): Nothing = noImpl
    fun setHistory(history: dynamic): Nothing = noImpl
    fun getHistory(): dynamic = noImpl
    fun clearHistory(): Nothing = noImpl
    fun cursorCoords(): dynamic = noImpl
    fun clearGutter(gutter: String): Nothing = noImpl
    fun markText(start: Position, end: Position, json: Json): Any = noImpl
    fun setGutterMarker(line: Int, gutter: String, element: HTMLElement): Nothing = noImpl
    fun lineInfo(line: Int): dynamic = noImpl
    fun lineCount(): Int = noImpl
    fun indentLine(lineNo: Int): Nothing = noImpl
    fun operation(function: () -> Unit): Nothing = noImpl
    fun swapDoc(document: Doc): Nothing = noImpl
    fun openDialog(template: HTMLElement, callback: () -> Unit, options: dynamic): (() -> Unit) = noImpl
    fun addLineWidget(lineNo: Int, help: HTMLElement?, options: Json): Nothing = noImpl
    fun setSelection(anchor: Position, head: Position): Nothing = noImpl
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