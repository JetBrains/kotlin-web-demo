package ii_conventions

fun addIndex(list: List<Int>): List<Int>{
    val result = arrayListOf<Int>()
    for((index, value) in list.withIndex())
        result.add(value + index)
    return result
}