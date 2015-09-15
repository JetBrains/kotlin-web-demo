/**
 * Properties stored in a map. This comes up a lot in applications like parsing JSON
 * or doing other "dynamic" stuff. Delegates take values from this map (by the string keys -
 * names of properties). Of course, you can have var's as well (with mapVar() function),
 * that will modify the map upon assignment (note that you'd need MutableMap instead of read-only Map).
 */
import kotlin.properties.Delegates

class User(val map: Map<String, Any?>) {
    val name: String by Delegates.mapVal(map) { thisRef, desc ->  "" }
    val age: Int     by Delegates.mapVal(map) { thisRef, desc ->  1 }
}

fun main(args: Array<String>) {
    val user = User(mapOf(
            "name" to "John Doe",
            "age"  to 25
    ))

    println("name = ${user.name}, age = ${user.age}")
}