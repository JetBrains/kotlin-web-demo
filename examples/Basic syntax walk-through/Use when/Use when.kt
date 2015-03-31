fun main(args: Array<String>) {
    cases("Hello")
    cases(1)
    cases(3)
    cases(System.currentTimeMillis())
    cases(MyClass())
    cases("hello")
}

fun cases(obj: Any) {
    when (obj) {
        1 -> println("One")
        in 2..4 -> println("Between two and four")
        "Hello" -> println("Greeting")
        is Long -> println("Long")
        !is String -> println("Not a string")
        else -> println("Unknown")
    }
}

class MyClass() {
}
