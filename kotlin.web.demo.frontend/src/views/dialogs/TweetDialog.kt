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
import kotlinx.html.js.onInputFunction
import org.w3c.dom.*
import utils.jquery.jq
import utils.jquery.ui.Dialog
import utils.twitter.Twitter
import kotlin.browser.document
import kotlin.browser.window

object TweetDialog {
    private val MAX_TWEET_LENGTH = 116
    val element: HTMLDivElement = document.body!!.append.div {
        title = "Congratulations!"
        img {
            id = "level-gif"
            width = "100%"
            height = "250px"
        }
        form {
            id="tweet-form"
            action = "/twitter/login"
            classes = setOf("tweet-form")
            textArea {
                classes = setOf("tweet-content")
                name = "tweet-text"
                onInputFunction = {
                    updateCounter()
                }
            }
            input {
                type = InputType.hidden
                id = "level-input-field"
                name = "kotlin-level"
                value = "1"
            }
            div {
                classes = setOf("buttonset")
                div {
                    + "140"
                    classes = setOf("charsleft-counter")
                }
                input {
                    value = "Tweet"
                    type = InputType.submit
                    classes = setOf("button tweet-button")
                }
            }
        }
    }

    val counter = jq(element).find(".charsleft-counter")[0]
    val tweetForm = jq(element).find(".tweet-form")[0] as HTMLFormElement
    val tweetContentInput = jq(element).find(".tweet-content")[0] as HTMLTextAreaElement
    val levelGif = jq(element).find("#level-gif")[0] as HTMLImageElement
    val levelInput = jq(element).find("#level-input-field")[0] as HTMLInputElement

    init {
        tweetForm.onsubmit = {
            if(Twitter.text.getTweetLength(tweetContentInput.value) > MAX_TWEET_LENGTH){
                window.alert("Tweet message can't be longer than $MAX_TWEET_LENGTH characters.")
                false
            } else{
                undefined
            }
        }
    }

    val dialog = Dialog(
            element,
            autoOpen = false,
            resizable = false,
            modal = true,
            width = 500,
            dialogClass = "tweet-dialog lightweight-dialog"
    )

    fun open(level: Int ){
        levelInput.value = level.toString()
        levelGif.src = "static/images/${level}level.gif"
        tweetContentInput.value = "Hey, I just completed level $level of Kotlin Koans. http://try.kotl.in/koans #kotlinkoans"
        updateCounter()
        dialog.open()
    }

    private fun updateCounter(){
        counter.textContent = (MAX_TWEET_LENGTH - Twitter.text.getTweetLength(tweetContentInput.value)).toString()
    }

}