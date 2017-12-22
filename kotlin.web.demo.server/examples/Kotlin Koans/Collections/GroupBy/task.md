## Group By

Implement `Shop.groupCustomersByCity()` using
[`groupBy`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/kotlin.-iterable/group-by.html).

```kotlin
val result = listOf("a", "b", "ba", "ccc", "ad").groupBy { it.length }
result == mapOf(1 to listOf("a", "b"), 2 to listOf("ba", "ad"), 3 to listOf("ccc"))
```