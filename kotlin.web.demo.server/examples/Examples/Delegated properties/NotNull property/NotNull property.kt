/**
 * Users frequently ask what to do when you have a non-null var, but you don't have an
 * appropriate value to assign to it in constructor (i.e. it must be assigned later)?
 * You can't have an uninitialized non-abstract property in Kotlin. You could initialize it
 * with null, bit then you'd have to check every time you access it. Now you have a delegate
 * to handle this. If you read from this property before writing to it, it throws an exception,
 * after the first assignment it works as expected.
 */

import kotlin.properties.Delegates

class User {
    var name: String by Delegates.notNull()

    fun init(name: String) {
        this.name = name
    }
}

fun main(args: Array<String>) {
    val user = User()
    // user.name -> IllegalStateException
    user.init("Carl")
    println(user.name)
}