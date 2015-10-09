## Sum

Implement `Customer.getTotalOrderPrice()` using
[`sum`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sum.html) or
[`sumBy`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sum-by.html).

```kotlin
listOf(1, 5, 3).sum() == 9
listOf("a", "b", "cc").sumBy { it.length() } == 4
```

If you want to sum the double values, use
[`sumByDouble`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sum-by-double.html)