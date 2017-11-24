package sum

fun sum(a: IntArray): Int {
    return a.fold(0, { a, b -> a + b })
}