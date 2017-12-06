<answer>operator fun MyDate.rangeTo(other: MyDate) = DateRange(this, other)</answer>

class DateRange(override val start: MyDate, override val endInclusive: MyDate): ClosedRange<MyDate>

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in first..last
}