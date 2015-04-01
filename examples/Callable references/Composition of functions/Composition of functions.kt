/**
 * The composition function return a composition of two functions passed to it:
 * compose(f, g) = f(g(*)).
 * Now, you can apply it to callable references.
 */

fun main(args: Array<String>) {
    val oddLength = compose(::isOdd, ::length)
    val strings = listOf("a", "ab", "abc")
    println(strings.filter(oddLength))
}

fun isOdd(x: Int) = x % 2 != 0
fun length(s: String) = s.length()

fun compose<A, B, C>(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { x -> f(g(x)) }
}
