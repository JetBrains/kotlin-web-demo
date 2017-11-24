/**
 *  Data class gets component functions, one for each property declared
 *  in the primary constructor, generated automatically, same for all the
 *  other goodies common for data: toString(), equals(), hashCode() and copy().
 *  See http://kotlinlang.org/docs/reference/data-classes.html#data-classes
 */

data class User(val name: String, val id: Int)

fun getUser(): User {
    return User("Alex", 1)
}

fun main(args: Array<String>) {
    val user = getUser()
    println("name = ${user.name}, id = ${user.id}")

    // or

    val (name, id) = getUser()
    println("name = $name, id = $id")

    // or

    println("name = ${getUser().component1()}, id = ${getUser().component2()}")
}
