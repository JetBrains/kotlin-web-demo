## Sum

Implement `Customer.getTotalOrderPrice()` using
[`sum`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/kotlin.-iterable/sum.html) or
[`sumBy`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/kotlin.-iterable/sum-by.html).

```kotlin
listOf(1, 5, 3).sum() == 9
listOf("a", "b", "cc").sumBy { it.length } == 4
```

If you want to sum the double values, use
[`sumByDouble`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/kotlin.-iterable/sum-by-double.html).