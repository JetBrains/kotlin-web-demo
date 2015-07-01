package iv_builders.examples

fun <T, R> with2(receiver: T, f: T.() -> R): R = throw Exception("Not implemented")

fun buildString(): String {
    val stringBuilder = StringBuilder()
    with2 (stringBuilder) {
        append("Numbers: ")
        for (i in 1..10) {
            append(i)
        }
    }
    return stringBuilder.toString()
}

fun buildMap(): Map<Int, String> {
    val map = hashMapOf<Int, String>()
    with2 (map) {
        put(0, "0")
        for (i in 1..10) {
            put(i, "$i")
        }
    }
    return map
}
