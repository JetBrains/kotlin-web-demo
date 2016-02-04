## For loop

Kotlin [for loop](http://kotlinlang.org/docs/reference/control-flow.html#for-loops)
iterates through anything that provides an iterator.
Make the class `DateRange` implement [`Iterable<MyDate>`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-iterable/),
so that it could be iterated over.
You can use the function `MyDate.nextDay()` defined in [DateUtil.kt](/#/Kotlin%20Koans/Conventions/For%20loop/DateUtil.kt)