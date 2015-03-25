package maxindex

fun indexOfMax(a: IntArray): Int? {
    return a.indices.reverse().maxBy { a[it] } ?: null
}