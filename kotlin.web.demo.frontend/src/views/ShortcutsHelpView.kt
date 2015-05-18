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

package views

import jquery.jq
import jquery.ui.dialog
import kotlin.browser.document
import html4k.*
import html4k.js.*
import html4k.dom.*
import kotlin.dom.eventHandler

/**
 * Created by Semyon.Atamas on 5/18/2015.
 */

class ShortcutsHelpView() {
    fun open() {
        jq(dialogElement).dialog("open");
    }

    fun addShortcut(keyNames: Array<String>, description: String) {
        var shortcutElement = shortcutsHelpElement.append.tr{}

        var shortcutKeyCombinationElement = shortcutElement.append.td{
            classes = setOf("shortcutKeyCombination")
        }

        var separator = "";
        for (keyName in  keyNames.reverse()) {
            if (separator != "") {
                shortcutKeyCombinationElement.append.div {
                    + separator
                    classes = setOf("shortcutSeparator")
                }
            }
            shortcutKeyCombinationElement.append.div {
                + keyName
                classes = setOf("shortcutKeyName")
            }
            separator = "+";
        }

        shortcutElement.append.td {
            div {
                + description
                classes = setOf("shortcutDescription")
            }
        }
    }

    var dialogElement = document.body!!.append.div {
        title = "Help"
    }
    var shortcutsHelpElement = dialogElement.append.div {
        id = "shortcuts-help"
    }
    init {
        jq(dialogElement).keydown({ event ->
            if (event.keyCode == 27 || event.keyCode == 13) {
                /*escape enter*/
                jq(dialogElement).dialog("close");
            }
            event.stopPropagation();
        }) ;

        jq(dialogElement).dialog(
                json(
                    "resizable" to false,
                    "minWidth" to 500,
                    "autoOpen" to false,
                    "modal" to true
                )
        ) ;
    }
}