package ch05.ex4_2_SAMConstructorsExplicitConversionOfLambdasToFunctionalInterfaces

fun createAllDoneRunnable(): Runnable {
    return Runnable { println("All done!") }
}

fun main(args: Array<String>) {
    createAllDoneRunnable().run()
}
