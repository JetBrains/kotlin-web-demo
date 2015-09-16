## Fold

Implement `Shop.getProductsOrderedByAllCustomers()` using
[`fold`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/fold.html).

```kotlin
listOf(1, 2, 3, 4).fold(1, {
    partProduct, element ->
    element * partProduct
}) == 24
```