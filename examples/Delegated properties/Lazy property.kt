import kotlin.properties.Delegates

class LazySample {
  val lazy: String by Delegates.lazy {
    println("computed!")
    "my lazy"
  }
}

fun main(args: Array<String>) {
  val sample = LazySample()
  println("lazy = ${sample.lazy}")
  println("lazy = ${sample.lazy}")
}