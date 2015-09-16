import java.util.HashMap

fun buildMap<K, V>(build: HashMap<K, V>.() -> Unit): Map<K, V>{
    val map = HashMap<K, V>()
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