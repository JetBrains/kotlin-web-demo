package iv_builders

fun buildMap<K, V>(build: MutableMap<K, V>.() -> Unit): Map<K, V>{
    val map = hashMapOf<K, V>()
    map.build()
    return map
}