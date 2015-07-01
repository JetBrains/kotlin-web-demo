##All, Any and other predicates

Implement all this functions using the following methods:
[all](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/all.html),
[any](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/any.html),
[count](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/count.html),
[firstOrNull](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/find.html).

`all()` returns true if all elements match the given predicate.

`any()` returns true if at least one element match the given predicate.

`count()` returns the number of elements matching the given predicate.

`firstOrNull()` returns the first element matching the given predicate, or null if element was not found.

```kotlin
val numbers = listOf(-1, 0, 2)
val isZero: (Int) -> Boolean = { it == 0 }
numbers.any(isZero) == true
numbers.all(isZero) == false
numbers.count(isZero) == 1
numbers.firstOrNull { it > 0 } == 2
```