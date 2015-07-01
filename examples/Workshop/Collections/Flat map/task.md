#Flat map
Implement extension functions `Customer.orderedProducts()` and `Shop.allOrderedProducts()` using method
[flatMap](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/flat-map.html).

`flatMap()` returns a single list of all elements yielded from results of transform function
being invoked on each element of original collection.

```kotlin
val result = listOf("abc", "12").flatMap { it.toCharList() }
result == listOf('a', 'b', 'c', '1', '2')
```