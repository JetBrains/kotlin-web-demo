import kotlin.properties.Delegates

class Example {
  var p: String by Delegate()

  override fun toString() = "Example Class"
}

class Delegate() {
  fun get(thisRef: Any?, prop: PropertyMetadata): String {
    return "$thisRef, thank you for delegating '${prop.name}' to me!"
  }

  fun set(thisRef: Any?, prop: PropertyMetadata, value: String) {
    println("$value has been assigned to ${prop.name} in $thisRef")
  }
}

fun main(args: Array<String>) {
  val e = Example()
  println(e.p)
  e.p = "NEW"
}