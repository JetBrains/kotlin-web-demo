package runs

fun runs(a: IntArray): Int {
    if (a.isEmpty()) return 0
    var prev = a[0]
    var runs = 1
    a.forEach({ a ->
        if (a != prev) {
            runs++
        }
        prev = a
    })
    return runs
}