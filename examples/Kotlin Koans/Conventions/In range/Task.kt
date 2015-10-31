class DateRange(override val start: MyDate, override val end: MyDate) : Range<MyDate> {
}

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in DateRange(first, last)
}