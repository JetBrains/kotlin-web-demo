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
import kotlinx.html.js.*
import kotlinx.html.dom.*
import jquery.jq
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import utils.KeyCode
import utils.jquery.ui.Dialog
import utils.jquery.ui.DialogButton
import views.dialogs.ValidationResult
import utils.jquery.ui.button
import utils.jquery.keydown
import utils.jquery.trigger
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
        classes = setOf("input-dialog-error-message")
    }

    private val dialog = Dialog(
            dialogElement,
            resizable = false,
            modal = true,
            width = 380,
            autoOpen = false,
            onOpen = { event, ui ->
                inputElement.select()
            }
    )

    init {
        jq(dialogElement).keydown({ event ->
            when (event.keyCode) {
                KeyCode.ENTER.code -> {
                    val okButton = getDialogButton(dialogElement, 1)
                    okButton.trigger("click")
                }
            }
            event.stopPropagation()
        })
    }

    private fun processValidationResult(result: ValidationResult) {
        val okButton = getDialogButton(dialogElement, 1)
        okButton.button("option", "disabled", !result.valid)
        errorMessageElement.innerHTML = if (result.valid) "" else result.message
        inputElement.style.outlineColor = if (result.valid) "" else "red"
        inputElement.focus()
    }

    private fun getVerifiedDefaultValue(defaultValue: String, validate: (String) -> ValidationResult): String {
        var verifiedDefaultValue = defaultValue
        var i = 1
        while (!validate(verifiedDefaultValue).valid) {
            verifiedDefaultValue = defaultValue + i
            ++i
        }
        return verifiedDefaultValue
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
            processValidationResult(validate(inputElement.value))
        }
        inputElement.value = getVerifiedDefaultValue(defaultValue, validate)
        inputElement.select()

        inputLabelElement.textContent = inputLabel

        dialog.title = title
        dialog.buttons = arrayOf(
                DialogButton(
                        text = okButtonCaption,
                        click = { event: Event ->
                            event.stopPropagation()
                            callback(inputElement.value)
                            dialog.close()
                        }
                ),
                DialogButton(
                        text = "Cancel",
                        click = { event: Event ->
                            event.stopPropagation()
                            dialog.close()
                        }
                )
        )
        dialog.open()
    }
}

data class ValidationResult(val valid: Boolean, val message: String = "")