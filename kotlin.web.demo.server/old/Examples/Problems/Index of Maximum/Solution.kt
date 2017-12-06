package maxindex

fun indexOfMax(a: IntArray): Int? {
    return a.indices.reversed().maxBy { a[it] } ?: null
}