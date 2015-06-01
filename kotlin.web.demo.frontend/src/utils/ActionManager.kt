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

package utils

import kotlin.browser.window

public class ActionManager(
        private val defaultActionShortcutMap: Map<String, Shortcut>,
        private val macActionShortcutMap: Map<String, Shortcut>
) {

    private val NEVER_PRESSED_SHORTCUT = Shortcut(arrayOf(""), { false; });

    val shortcutMap = if (window.navigator.appVersion.indexOf("Mac") != -1) macActionShortcutMap else defaultActionShortcutMap

    fun getShortcut(id: String): Shortcut =
            shortcutMap.get(id) ?: NEVER_PRESSED_SHORTCUT

//    fun registerAction(actionName: String, defaultShortcut: Shortcut, macShortcut: Shortcut?) {
//        macActionShortcutMap.put(actionName, macShortcut ?: defaultShortcut);
//        defaultActionShortcutMap.put(actionName, defaultShortcut);
//    }
}