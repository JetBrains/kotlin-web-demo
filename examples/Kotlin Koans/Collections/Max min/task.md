##Max min
Implement extension functions `Shop.getCustomerWithMaximumNumberOfOrders()` and `Customer.getMostExpensiveOrderedProduct()` using
methods
[max](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/max.html) and
[min](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/min.html), or
[maxBy](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/max.html) and
[minBy](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/min-by.html)

`max()` and `min()` methods returns the largest and the smallest element in collection or null if there are no elements.

`maxBy()` and `minBy()` methods returns the first element yielding the smallest value of the given function
or null if there are no elements.

```kotlin
listOf(1, 42, 4).max() == 42
listOf("a", "ab").minBy { it.length() } == "a"
```
