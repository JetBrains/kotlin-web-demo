## In range

In Kotlin `in` checks are translated to the corresponding `contains` calls:

```kotlin
val list = listOf("a", "b")
"a" in list  // list.contains("a")
"a" !in list // !list.contains("a")
```

Read about [ranges](http://kotlinlang.org/docs/reference/ranges.html).
Make the class `DateRange` implement the standard
[`ClosedRange`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-closed-range/index.html)
interface to allow `in` checks with a range of dates.