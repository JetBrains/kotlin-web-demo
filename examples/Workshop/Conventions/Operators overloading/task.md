## Operators overloading

Implement a kind of date arithmetic, support adding years, weeks and days to a date.
You could be able to write the code like this: `date + YEAR * 2 + WEEK * 3 + DAY * 15`.

At first, add an extension function 'plus()' to MyDate, taking TimeInterval as an argument.
Use an utility function `MyDate.addTimeInterval()` declared in
[DateUtil.kt](/#/Workshop/Conventions/Operators%20overloading/DateUtil.kt)

Then, try to support adding several time intervals to a date.
You may need an extra class.