class DateRange(override public val start: MyDate, override public val end: MyDate) : Range<MyDate> {
}

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in DateRange(first, last)
}