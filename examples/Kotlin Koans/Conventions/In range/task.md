## In range

In Kotlin `in` checks are translated to the corresponding `contains` calls:

```kotlin
val list = listOf("a", "b")
"a" in list  // list.contains("a")
"a" !in list // !list.contains("a")
```

Read about [ranges](http://kotlinlang.org/docs/reference/ranges.html).
Add a method `fun contains(d: MyDate)` to the class `DateRange` to allow `in` checks with a range of dates.