##Sort
Implement extension function `Shop.getCustomersSortedByNumberOfOrders()` using method
[sort](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort.html) or
[sortBy](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort-by.html).

`sort()` returns a sorted list of all elements.

`sortBy()` returns a sorted list of all elements, ordered by results of specified order function.

```kotlin
listOf("bbb", "a", "cc").sort() == listOf("a", "bbb", "cc")
listOf("bbb", "a", "cc").sortBy { it.length() } == listOf("a", "cc", "bbb")
```