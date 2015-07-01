package ii_conventions

class DateRange(public val start: MyDate, public val end: MyDate): Iterable<MyDate>{
    override fun iterator(): Iterator<MyDate> = throw Exception("Not implemented")
}

class DateIterator() : Iterator<MyDate> {
    override fun next() : MyDate{
        throw Exception("Not implemented")
    }
    override fun hasNext() : Boolean {
        throw Exception("Not implemented")
    }
}

fun iterateOverDateRange(firstDate: MyDate, secondDate: MyDate, handler: (MyDate) -> Unit) {
    for (date in DateRange(firstDate, secondDate)) {
        handler(date)
    }
}