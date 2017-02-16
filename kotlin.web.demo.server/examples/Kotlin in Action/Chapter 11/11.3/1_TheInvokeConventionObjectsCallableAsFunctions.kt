package ch11.ex3_1_TheInvokeConventionObjectsCallableAsFunctions

class Greeter(val greeting: String) {
    operator fun invoke(name: String) {
        println("$greeting, $name!")
    }
}

fun main(args: Array<String>) {
    val bavarianGreeter = Greeter("Servus")
    bavarianGreeter("Dmitry")
}
