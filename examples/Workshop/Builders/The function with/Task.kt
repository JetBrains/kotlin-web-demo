fun <T, R> myWith(receiver: T, f: T.() -> R): R = TODO()

fun buildString(): String {
    val stringBuilder = StringBuilder()
    myWith (stringBuilder) {
        append("Numbers: ")
        for (i in 1..10) {
            append(i)
        }
    }
    return stringBuilder.toString()
}

fun buildMap(): Map<Int, String> {
    val map = hashMapOf<Int, String>()
    myWith (map) {
        put(0, "0")
        for (i in 1..10) {
            put(i, "$i")
        }
    }
    return map
}
