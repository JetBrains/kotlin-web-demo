package ch03.ex4_2_VarargsFunctionsThatAcceptAnArbitraryNumberOfArguments

fun main(args: Array<String>) {
    val list = listOf("args: ", *args)
    println(list)
}
