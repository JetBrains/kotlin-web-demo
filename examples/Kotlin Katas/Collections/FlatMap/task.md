## FlatMap

Implement `Customer.getOrderedProducts()` and `Shop.getAllOrderedProducts()` using
[`flatMap`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/flat-map.html).

```kotlin
val result = listOf("abc", "12").flatMap { it.toCharList() }
result == listOf('a', 'b', 'c', '1', '2')
```