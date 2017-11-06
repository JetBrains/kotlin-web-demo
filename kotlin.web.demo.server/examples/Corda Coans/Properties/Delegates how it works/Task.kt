import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class D {
    var date: MyDate by EffectiveDate()
}

class EffectiveDate<R> : ReadWriteProperty<R, MyDate> {

    var timeInMillis: Long? = null

    override fun getValue(thisRef: R, property: KProperty<*>): MyDate {
        <taskWindow>TODO()</taskWindow>
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: MyDate) {
        <taskWindow>TODO()</taskWindow>
    }
}

