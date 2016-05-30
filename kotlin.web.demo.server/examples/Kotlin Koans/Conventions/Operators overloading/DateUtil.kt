import java.util.Calendar

fun MyDate.addTimeIntervals(timeInterval: TimeInterval, number: Int): MyDate {
    val c = Calendar.getInstance()
    c.set(year + if (timeInterval == TimeInterval.YEAR) number else 0, month, dayOfMonth)
    var timeInMillis = c.getTimeInMillis()
    val millisecondsInADay = 24 * 60 * 60 * 1000L
    timeInMillis += number * when (timeInterval) {
        TimeInterval.DAY -> millisecondsInADay
        TimeInterval.WEEK -> 7 * millisecondsInADay
        TimeInterval.YEAR -> 0L
    }
    val result = Calendar.getInstance()
    result.setTimeInMillis(timeInMillis)
    return MyDate(result.get(Calendar.YEAR), result.get(Calendar.MONTH), result.get(Calendar.DATE))
}