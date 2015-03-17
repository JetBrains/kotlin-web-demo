fun main(args : Array<String>) {
  println(Any::class.simpleName)
  println(A::class.simpleName)
  println(A::class.getProperties().first().name)
}

class A{
  val x = 0
}