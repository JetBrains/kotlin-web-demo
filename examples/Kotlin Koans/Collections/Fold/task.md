##Fold
Implement extension function `Shop.getProductsOrderedByAllCustomers()` using method
[fold](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/fold.html).

`fold()` accumulates value starting with initial value and applying operation from left
to right to current accumulator value and each element.

```kotlin
listOf(1, 2, 3, 4).fold(1, { partProduct, element -> element * partProduct }) == 24
```