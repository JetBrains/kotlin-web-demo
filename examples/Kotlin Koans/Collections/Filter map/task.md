## Filter; map

Implement extension functions `Shop.getCitiesCustomersAreFrom()` and `Shop.getCustomersFrom()` using functions
[`map`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/map.html) and
[`filter`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/filter.html).

```kotlin
val numbers = listOf(1, -1, 2)
numbers.filter { it > 0 } == listOf(1, 2)
numbers.map { it * it } == listOf(1, 1, 4)
```