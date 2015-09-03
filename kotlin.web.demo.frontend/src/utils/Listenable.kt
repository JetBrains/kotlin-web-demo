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

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

data class ChangeEvent<T>(
        val oldValue: T,
        val newValue: T
)

class Listenable<T: Any>(initialValue: T, private val varListener: VarListener<T>) : ReadWriteProperty<Any?, T> {
    private var value = initialValue
    init {
        varListener.setInitialValue(initialValue)
    }

    override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        if(this.value != value) {
            val e = ChangeEvent(this.value, value)
            this.value = value
            varListener.modified(e)
        }
    }

    override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        return value
    }
}

class VarListener<T: Any> {
    private var value: T by Delegates.notNull()
    private val allListeners = arrayListOf <(ChangeEvent<T>) -> Unit>()
    private val namedListeners = hashMapOf<String, (ChangeEvent<T>) -> Unit>()

    fun setInitialValue(initialValue: T){
        value = initialValue
    }

    fun modified(e: ChangeEvent<T>) {
        allListeners.forEach { it(e) }
    }

    fun addModifyListener(callback: (ChangeEvent<T>) -> Unit) {
        allListeners.add(callback)
    }

    fun addModifyListener(name: String, callback: (ChangeEvent<T>) -> Unit) {
        namedListeners.put(name, callback)
        allListeners.add(callback)
    }

    fun removeNotifyListener(name: String){
        var listener = namedListeners.get(name)
        if (listener != null){
            namedListeners.remove(name)
            allListeners.remove(name)
        } else{
            throw IllegalArgumentException("Listener with name $name not found")
        }
    }
}