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
import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import utils.KeyCode
import utils.jquery.jq
import utils.jquery.ui.Dialog
import utils.jquery.ui.DialogButton
import kotlin.browser.document


open class InputDialogView(
        val dialogTitle: String,
        val inputLabel: String,
        val okButtonCaption: String,
        val defaultValue: String,
        val validate: (String) -> ValidationResult,
        val callback: (UserInput) -> Unit,
        val submitOnEnter: Boolean = true
) {
    private val dialogElement = render()

    private val errorMessageElement = jq(dialogElement).find(".input-dialog-error-message")[0]

    protected val inputElement = jq(dialogElement).find("#input-dialog-input")[0] as HTMLInputElement

    private val dialog: Dialog = Dialog(
            dialogElement,
            resizable = false,
            modal = true,
            width = 380,
            onOpen = { event, ui ->
                inputElement.select()
            },
            onClose = { this.close() },
            buttons = arrayOf(
                    DialogButton(
                            text = okButtonCaption,
                            click = { event: Event ->
                                event.stopPropagation()
                                callback(getUserInput())
                                this.close()
                            }
                    ),
                    DialogButton(
                            text = "Cancel",
                            click = { event: Event ->
                                event.stopPropagation()
                                this.close()
                            }
                    )
            )
    )

    init {
        if(submitOnEnter) {
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

        inputElement.oninput = {
            processValidationResult(validate(inputElement.value))
        }
        inputElement.select()
    }

    open fun render() = document.create.div {
        id = "input-dialog"
        title = dialogTitle
        span {
            +inputLabel
            id = "input-dialog-text"
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

    protected open fun getUserInput() = UserInput(inputElement.value)

    fun close() {
        dialog.destroy()
        dialogElement.parentNode?.removeChild(dialogElement);
    }

    private fun processValidationResult(result: ValidationResult) {
        val okButton = getDialogButton(dialogElement, 1)
        okButton.button("option", "disabled", !result.valid)
        errorMessageElement.innerHTML = if (result.valid) "" else result.message
        inputElement.style.outlineColor = if (result.valid) "" else "red"
        inputElement.focus()
    }

    protected fun getVerifiedDefaultValue(defaultValue: String, validate: (String) -> ValidationResult): String {
        var verifiedDefaultValue = defaultValue
        var i = 1
        while (!validate(verifiedDefaultValue).valid) {
            verifiedDefaultValue = defaultValue + i
            ++i
        }
        return verifiedDefaultValue
    }
}

open class UserInput(val value: String);

data class ValidationResult(val valid: Boolean, val message: String = "")