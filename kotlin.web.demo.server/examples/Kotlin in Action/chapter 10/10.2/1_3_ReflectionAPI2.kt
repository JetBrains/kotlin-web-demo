package ch10.ex2_1_3_ReflectionAPI2

var counter = 0

/**
 * NOTE: This example uses reflection, which is not supported in the "Try Kotlin" online environment.
 * If you'd like to run it, please download the source code from https://www.manning.com/books/kotlin-in-action
 * and run it on your local machine.
 */
fun main(args: Array<String>) {
    val kProperty = ::counter
    kProperty.setter.call(21)
    println(kProperty.get())
}
