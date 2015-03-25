package pairless

fun findPairless(a: IntArray): Int {
    return a.fold(0, {(a, b) -> a.xor(b) })
}