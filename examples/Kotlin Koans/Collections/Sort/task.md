## Sort

Implement `Shop.getCustomersSortedByNumberOfOrders()` using
[sort](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort.html) or
[sortBy](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort-by.html).

```kotlin
listOf("bbb", "a", "cc").sort() == listOf("a", "bbb", "cc")
listOf("bbb", "a", "cc").sortBy { it.length() } == listOf("a", "cc", "bbb")
```