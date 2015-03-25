import kotlin.properties.Delegates

class User(val map: Map<String, Any?>) {
    val name: String by Delegates.mapVal(map)
    val age: Int     by Delegates.mapVal(map)
}

fun main(args: Array<String>) {
    val user = User(mapOf(
            "name" to "John Doe",
            "age"  to 25
    ))

    println("name = ${user.name}, age = ${user.age}")
}