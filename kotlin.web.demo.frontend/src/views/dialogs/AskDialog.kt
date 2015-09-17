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
import kotlinx.html.dom.append
import kotlinx.html.js.div
import utils.jquery.ui.Dialog
import kotlin.browser.document

object AskDialog {
    val element = document.body!!.append.div {
        classes = setOf("ask-dialog")
        title = "Ask a question"
        a {
            +"StackOverflow"
            href = "http://stackoverflow.com/questions/ask?tags=kotlin"
            target = "_blank"
        }
        a{
            +"Devnet"
            href = "https://devnet.jetbrains.com/community/kotlin"
            target = "_blank"
        }
        form {
            action = "/kotlinServer?type=sendMail"
            method = FormMethod.post
            section {
                label {
                    + "Name: "
                    for_ = "ask-form-name"
                }
                input {
                    type = InputType.text
                    name = "name"
                    id = "ask-form-name"
                }
            }

            section {
                label {
                    + "Email: "
                    for_ = "ask-form-email"
                }
                input {
                    type = InputType.email
                    name = "email"
                    id = "ask-form-email"
                }
            }

            section {
                label {
                    + "Subject: "
                    for_ = "ask-form-email"
                }
                input {
                    type = InputType.text
                    name = "subject"
                    id = "ask-subject"
                }
            }

            section {
                label {
                    + "Question: "
                    for_ = "ask-form-question"
                }
                textArea {
                    id = "ask-form-question"
                    name = "question"
                }
            }

            section {
                input {
                    classes = setOf("submit-button")
                    type = InputType.submit
                    value = "Submit"
                }
            }
        }
    }

    val dialog = Dialog(element, autoOpen = false, modal = true)

    fun open(){
        dialog.open()
    }
}