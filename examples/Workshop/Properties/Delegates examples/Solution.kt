package iii_properties

import kotlin.properties.Delegates

class Commodity(data: MutableMap<String, Any?>) {
    val description: String by Delegates.mapVal(data)
    var price: Int by Delegates.mapVar(data)
    var isAvailable: Boolean by Delegates.mapVar(data)
}