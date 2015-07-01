package ii_conventions

class DateRange(override public val start: MyDate, override public val end: MyDate): Iterable<MyDate>, Range<MyDate>{
    override fun iterator(): Iterator<MyDate> = DateIterator(this)
    override fun contains(item: MyDate): Boolean = throw Exception("Not implemented")
}

fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return date in first..last
}