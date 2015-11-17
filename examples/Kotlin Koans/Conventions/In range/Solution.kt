<answer>class DateRange(override val start: MyDate, override val endInclusive: MyDate): ClosedRange<MyDate> {
    override fun contains(item: MyDate): Boolean = start < item && item < endInclusive
}</answer>

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in DateRange(first, last)
}