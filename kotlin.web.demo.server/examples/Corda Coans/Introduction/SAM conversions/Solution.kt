import java.util.*

fun getList(): List<Int> {
    val arrayList = arrayListOf(1, 5, 2)
    <answer>Collections.sort(arrayList, { x, y -> y - x })</answer>
    return arrayList
}