## FlatMap

Implement `Customer.getOrderedProducts()` and `Shop.getAllOrderedProducts()` using
[`flatMap`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/kotlin.-iterable/flat-map.html).

```kotlin
val result = listOf("abc", "12").flatMap { it.toList() }
result == listOf('a', 'b', 'c', '1', '2')
```