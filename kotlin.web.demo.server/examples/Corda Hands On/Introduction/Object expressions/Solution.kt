import java.util.*

fun getList(): List<Int> {
    val arrayList = arrayListOf(1, 5, 2)

<answer>Collections.sort(arrayList, object : Comparator<Int> {
    override fun compare(x: Int, y: Int) = y - x
})</answer>

    return arrayList
}