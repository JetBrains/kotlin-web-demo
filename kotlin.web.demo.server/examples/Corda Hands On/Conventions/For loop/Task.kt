<taskWindow>class DateRange(val start: MyDate, val end: MyDate)</taskWindow>

fun iterateOverDateRange(firstDate: MyDate, secondDate: MyDate, handler: (MyDate) -> Unit) {
    for (date in firstDate..secondDate) {
        handler(date)
    }
}