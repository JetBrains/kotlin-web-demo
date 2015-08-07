##Group By
Implement extension function `Shop.groupCustomersByCity()` using method
[groupBy](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/group-by.html).

`groupBy()` returns a map of the elements in original collection grouped by the result of given toKey function.

```kotlin
val result = listOf("a", "b", "ba", "ccc", "ad").groupBy { it.length() }
result == mapOf(1 to listOf("a", "b"), 2 to listOf("ba", "ad"), 3 to listOf("ccc"))
```