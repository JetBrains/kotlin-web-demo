## Operators overloading

Implement a kind of date arithmetic. Support adding years, weeks and days to a date.
You could be able to write the code like this: `date + YEAR * 2 + WEEK * 3 + DAY * 15`.

At first, add an extension function 'plus()' to MyDate, taking a TimeInterval as an argument.
Use an utility function `MyDate.addTimeIntervals()` declared in
[DateUtil.kt](/#/Kotlin%20Koans/Conventions/Operators%20overloading/DateUtil.kt)

Then, try to support adding several time intervals to a date.
You may need an extra class.