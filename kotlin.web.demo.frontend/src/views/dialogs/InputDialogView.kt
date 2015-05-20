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

import KeyCode
import html4k.js.*
import html4k.InputType
import html4k.dom.append
import html4k.dom.create
import html4k.js.div
import html4k.js.input
import html4k.js.span
import jquery.jq
import jquery.ui.dialog
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import views.*
import kotlin.browser.document


object InputDialogView {
    private val dialogElement: HTMLDivElement = document.create.div {
        id = "input-dialog"
    }

    private val inputLabelElement = dialogElement.append.span {
        id = "input-dialog-text"
    }

    private val inputElement = dialogElement.append.input {
        id = "input-dialog-input"
        type = InputType.text
    }

    private val errorMessageElement = dialogElement.append.div {
        classes = setOf("input-dialog-error-message");
    }

    init {
        jq(dialogElement).dialog(json(
                "resizable" to false,
                "modal" to true,
                "width" to 380,
                "autoOpen" to false,
                "open" to {
                    inputElement.select()
                }
        ))

        jq(dialogElement).keydown({ event ->
            when (event.keyCode) {
                KeyCode.ENTER.code -> {
                    val okButton = getDialogButton(dialogElement, 1)
                    okButton.trigger("click");
                }
                KeyCode.ESCAPE.code -> close();
            }
            event.stopPropagation();
        });
    }

    private fun processValidationResult(result: ValidationResult) {
        val okButton = getDialogButton(dialogElement, 1)
        okButton.button("option", "disabled", !result.valid);
        errorMessageElement.innerHTML = if (result.valid) "" else result.message
        inputElement.style.outlineColor = if (result.valid) "" else "red";
        inputElement.focus();
    }

    private fun getVerifiedDefaultValue(defaultValue: String, validate: (String) -> ValidationResult): String {
        var verifiedDefaultValue = defaultValue;
        var i = 1;
        while (!validate(verifiedDefaultValue).valid) {
            verifiedDefaultValue = defaultValue + i;
            ++i;
        }
        return verifiedDefaultValue
    }

    fun close() {
        jq(dialogElement).dialog("close");
    }

    fun open(
            title: String,
            inputLabel: String,
            okButtonCaption: String,
            defaultValue: String,
            validate: (String) -> ValidationResult,
            callback: (String) -> Unit
    ) {
        inputElement.oninput = {
            processValidationResult(validate(inputElement.value));
        };
        inputElement.value = getVerifiedDefaultValue(defaultValue, validate);
        inputElement.select();

        jq(dialogElement).dialog("option", "title", title);
        inputLabelElement.textContent = inputLabel;
        jq(dialogElement).dialog("option", "buttons", arrayOf(
                json(
                        "text" to okButtonCaption,
                        "click" to { event: Event ->
                            event.stopPropagation()
                            callback(inputElement.value);
                            close()
                        }
                ),
                json(
                        "text" to "Cancel",
                        "click" to { event: Event ->
                            event.stopPropagation()
                            close()
                        }
                )
        )
        );
        jq(dialogElement).dialog("open");
    }
}