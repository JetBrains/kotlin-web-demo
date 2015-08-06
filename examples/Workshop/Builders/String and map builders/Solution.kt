fun buildMap<K, V>(build: MutableMap<K, V>.() -> Unit): Map<K, V>{
    val map = hashMapOf<K, V>()
    map.build()
    return map
}

fun usage(): Map<Int, String> {
    return buildMap {
        put(0, "0")
        for (i in 1..10) {
            put(i, "$i")
        }
    }
}