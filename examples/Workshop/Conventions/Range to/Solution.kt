package ii_conventions

fun MyDate.rangeTo(other: MyDate) = DateRange(this, other)

class DateRange(public val start: MyDate, public val end: MyDate): Iterable<MyDate>{
    override fun iterator(): Iterator<MyDate> = DateIterator(this)
}

fun getRange(start: MyDate, end: MyDate): DateRange = start..end