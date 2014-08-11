data class User(val name: String, val id: Int)

fun getUser(): User {
  return User("Alex", 1)
}

fun main(args: Array<String>) {
  val user = getUser()
  println("name = ${user.name}, id = ${user.id}")

  // or

  val (name, id) = getUser()
  println("name = $name, id = $id")
    
  // or
    
  println("name = ${getUser().component1()}, id = ${getUser().component2()}")
}
