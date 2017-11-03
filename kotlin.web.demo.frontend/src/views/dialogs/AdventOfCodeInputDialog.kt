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

package views.dialogs

import kotlinx.html.*
import kotlin.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.HTMLTextAreaElement
import kotlin.text.Regex

class AdventOfCodeInputDialog(
        dialogTitle: String,
        inputLabel: String,
        okButtonCaption: String,
        defaultValue: String,
        validate: (String) -> ValidationResult,
        callback: (UserInput) -> Unit
) : InputDialogView(dialogTitle, inputLabel, okButtonCaption, defaultValue, validate, callback, submitOnEnter = false) {
    val adventOfCodeInput = document.getElementById("advent-of-code-input") as HTMLTextAreaElement;

    override fun render() = document.create.div {
        id = "input-dialog"
        classes = setOf("advent-of-code-dialog")
        title = dialogTitle
        div {
            classes = setOf("form-wrapper")
            label {
                for_ = "input-dialog-input"
                +inputLabel
            }
            input {
                id = "input-dialog-input"
                type = InputType.text
                value = getVerifiedDefaultValue(defaultValue, validate)
            }
            div {
                classes = setOf("input-dialog-error-message")
            }
        }
        div {
            classes = setOf("textarea-label")
            +"Your puzzle input: "
        }
        textArea {
            id = "advent-of-code-input"
        }
    }

    override fun getUserInput(): AdventOfCodeInput {
        val codeInput = adventOfCodeInput.value;

        val inputFileContent = if (codeInput.contains('\n')) {
            val lines = adventOfCodeInput.value.split(Regex("\r?\n"));
            val inputFileContentBuilder = StringBuilder();
            inputFileContentBuilder.append("val input = listOf(")
            inputFileContentBuilder.append(lines.joinToString(separator = "\"\"\",\n\"\"\"", prefix = "\n\"\"\"", postfix = "\"\"\"\n"))
            inputFileContentBuilder.append(")")
            inputFileContentBuilder.toString()
        } else {
            "val input = \"\"\"$codeInput\"\"\""
        }
        return AdventOfCodeInput(inputElement.value, inputFileContent)
    }

}

class AdventOfCodeInput(value: String, val codeInput: String) : UserInput(value)
