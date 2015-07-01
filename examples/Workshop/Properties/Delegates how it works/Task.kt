package iii_properties

import kotlin.properties.ReadWriteProperty

class EffectiveDate<R> : ReadWriteProperty<R, MyDate> {
    var timeInMillis: Long? = null

    override fun get(thisRef: R, desc: PropertyMetadata): MyDate {
        throw Exception("Not implemented")
    }

    override fun set(thisRef: R, desc: PropertyMetadata, value: MyDate) {
        throw Exception("Not implemented")
    }
}

