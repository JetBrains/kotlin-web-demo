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

import org.w3c.dom.HTMLElement
import kotlin.dom.addClass
import kotlin.dom.removeClass

/**
 * Created by Semyon.Atamas on 8/25/2015.
 */

class Button(
        val buttonElement: HTMLElement,
        onClick: ()-> Unit,
        disabled: Boolean = false
) {
    var disabled = disabled
    private set

    init {
        if(disabled) disable()
        buttonElement.onclick = {
            if(!this.disabled) {
                onClick()
            }
        }
    }



    fun disable(){
        buttonElement.addClass("disabled")
        disabled = true
    }

    fun enable(){
        buttonElement.removeClass("disabled")
        disabled = false
    }
}