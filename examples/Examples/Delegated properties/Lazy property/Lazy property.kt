/**
 * Delegates.lazy() is a function that returns a delegate that implements a lazy property:
 * the first call to get() executes the lambda expression passed to lazy() as an argument
 * and remembers the result, subsequent calls to get() simply return the remembered result.
 * If you want thread safety, use blockingLazy() instead: it guarantees that the values will
 * be computed only in one thread, and that all threads will see the same value.
 */

class LazySample {
    val lazy: String by lazy {
        println("computed!")
        "my lazy"
    }
}

fun main(args: Array<String>) {
    val sample = LazySample()
    println("lazy = ${sample.lazy}")
    println("lazy = ${sample.lazy}")
}