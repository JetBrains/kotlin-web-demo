package ch05.ex2_3_2_GroupByConvertingAListToAMapOfGroups1

fun main(args: Array<String>) {
    val list = listOf("a", "ab", "b")
    println(list.groupBy(String::first))
}
