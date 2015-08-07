##Filter map

Implement extension functions `Shop.getCitiesCustomersAreFrom()` and `Shop.getCustomersFrom()` using methods
[map](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/map.html) and
[filter](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/filter.html).

`map()` returns a list obtained by applying the given transform function to each element of the original collection.

`filter()` returns a list containing all elements matching the given predicate.

```kotlin
val numbers = listOf(1, -1, 2)
// If lambda has one parameter, the corresponding argument can be accessed as 'it'
numbers.filter { it > 0 } == listOf(1, 2)
numbers.map { it * it } == listOf(1, 1, 4)
```