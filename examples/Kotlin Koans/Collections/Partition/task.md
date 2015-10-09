## Partition
Implement `Shop.getCustomersWithMoreUndeliveredOrdersThanDelivered()` using
[`partition`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/partition.html).

```kotlin
val numbers = listOf(1, 3, -4, 2, -11)
val (positive, negative) = numbers.partition { it > 0 }
positive == listOf(1, 3, 2)
negative == listOf(-4, -11)
```

Note that [multi-assignment](http://kotlinlang.org/docs/reference/multi-declarations.html) syntax is used in this example.