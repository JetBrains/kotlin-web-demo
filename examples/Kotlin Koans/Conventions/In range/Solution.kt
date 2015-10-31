<answer>class DateRange(override val start: MyDate, override val end: MyDate): Range<MyDate> {
    override fun contains(item: MyDate): Boolean = start < item && item < end
}</answer>

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in DateRange(first, last)
}