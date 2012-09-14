import java.util.TreeMap

fun main(args: Array<String>) {
  val map = TreeMap<String, Int>()
  map.put("one", 1)
  map.put("two", 2)
  
  for ((key, value) in map) { 
    println("key = ${key}, value = ${value}")
  }
}

fun <K, V> Map.Entry<K, V>.component1() = getKey()
fun <K, V> Map.Entry<K, V>.component2() = getValue()

