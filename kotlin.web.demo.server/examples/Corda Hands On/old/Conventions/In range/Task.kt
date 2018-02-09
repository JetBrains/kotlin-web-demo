class DateRange(val start: MyDate, val endInclusive: MyDate)

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in DateRange(first, last)
}