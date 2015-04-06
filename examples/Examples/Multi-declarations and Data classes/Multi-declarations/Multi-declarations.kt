/**
 * This example introduces a concept that we call mutli-declarations.
 * It creates multiple variable at once. Anything can be on the right-hand
 * side of a mutli-declaration, as long as the required number of component
 * functions can be called on it.
 * See http://kotlinlang.org/docs/reference/multi-declarations.html#multi-declarations
 */

fun main(args: Array<String>) {
    val pair = Pair(1, "one")

    val (num, name) = pair

    println("num = $num, name = $name")
}

class Pair<K, V>(val first: K, val second: V) {
    fun component1(): K {
        return first
    }

    fun component2(): V {
        return second
    }
}


