<answer>data class MyDate(val year: Int, val month: Int, val dayOfMonth: Int) : Comparable<MyDate>{
    override fun compareTo(other: MyDate): Int {
        if (year != other.year) return year - other.year
        if (month != other.month) return month - other.month
        return dayOfMonth - other.dayOfMonth
    }
}</answer>

fun compare(date1: MyDate, date2: MyDate) = date1 < date2