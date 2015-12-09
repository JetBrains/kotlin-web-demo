## Sort

Implement `Shop.getCustomersSortedByNumberOfOrders()` using
[`sorted`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sorted.html) or
[`sortedBy`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sorted-by.html).

```kotlin
listOf("bbb", "a", "cc").sorted() == listOf("a", "bbb", "cc")
listOf("bbb", "a", "cc").sortedBy { it.length } == listOf("a", "cc", "bbb")
```