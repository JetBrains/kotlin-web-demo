import java.util.Calendar

data class MyDate(val year: Int, val month: Int, val dayOfMonth: Int)

fun MyDate.toMillis(): Long {
    val c = Calendar.getInstance()
    c.set(year, month, dayOfMonth, 0, 0, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.getTimeInMillis()
}

fun Long.toDate(): MyDate {
    val c = Calendar.getInstance()
    c.setTimeInMillis(this)
    return MyDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE))
}