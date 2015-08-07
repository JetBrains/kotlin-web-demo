##Sum
Implement extension function `Customer.getTotalOrderPrice()` using method
[sum](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sum.html) or
[sumBy](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sum-by.html).

`sum()` returns the sum of all elements in the collection.

`sumBy()` returns the sum of all values produced by transform function from elements in the collection.

```kotlin
listOf(1, 5, 3).sum() == 9
listOf("a", "b", "cc").sumBy {it.length()} == 4
```