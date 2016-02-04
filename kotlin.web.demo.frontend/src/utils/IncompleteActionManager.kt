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

import kotlin.browser.localStorage


object IncompleteActionManager {

    fun registerAction(id: String, timePoint: String, onRegistered: dynamic, callback: dynamic) {
        if (id !in actions.keys) {
            actions[id] = json(
                    "timePoint" to timePoint,
                    "callback" to callback,
                    "onRegistered" to onRegistered
            )
        } else {
            throw Exception("You can't register actions with same id.")
        }
    }

    fun incomplete(id: String) {
        if (id in actions.keys) {
            incompleteActions.add(id)
            actions[id].onRegistered()
        } else {
            throw Exception("Action not registered")
        }
    }

    fun cancel(id: String) {
        incompleteActions.remove(id)
    }

    fun checkTimepoint(timePoint: String) {
        for (incompleteAction in incompleteActions) {
            if (actions[incompleteAction].timePoint == "on" + timePoint.capitalize()) {
                actions[incompleteAction].callback()
            }
        }
        //TODO
        incompleteActions.clear()
    }

    fun onBeforeUnload() {
        localStorage.setItem("incompleteActions", JSON.stringify(incompleteActions))
    }

    var actions = hashMapOf<String, dynamic>()
    var incompleteActions = JSON.parse<Array<String>>(localStorage.getItem("incompleteActions") ?: "[]").toMutableList()

    init {
        localStorage.removeItem("incompleteActions")
    }
}