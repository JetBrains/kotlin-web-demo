import kotlin.properties.ReadWriteProperty

class D {
    var date: MyDate by EffectiveDate()
}

class EffectiveDate<R> : ReadWriteProperty<R, MyDate> {

    var timeInMillis: Long? = null

    override fun get(thisRef: R, desc: PropertyMetadata): MyDate {
        TODO()
    }

    override fun set(thisRef: R, desc: PropertyMetadata, value: MyDate) {
        TODO()
    }
}

