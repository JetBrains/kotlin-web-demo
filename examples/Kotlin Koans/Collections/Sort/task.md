## Sort

Implement `Shop.getCustomersSortedByNumberOfOrders()` using
[`sort`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort.html) or
[`sortBy`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort-by.html).

```kotlin
listOf("bbb", "a", "cc").sorted() == listOf("a", "bbb", "cc")
listOf("bbb", "a", "cc").sortedBy { it.length() } == listOf("a", "cc", "bbb")
```