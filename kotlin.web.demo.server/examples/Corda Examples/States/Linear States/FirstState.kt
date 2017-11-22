/*
 *
 */
import import net.corda.core.contracts.State

fun main(args: Array<String>) {
    val mfs = MyFirstState()
    println(mfs)
}

data class MyFirstState(val whatever: String) : State {
    // This is a Plain Ole' class .. You can put whatever functions etc you want in here.
    // But they can be as basic as you want as well.. This one just has a string.
}