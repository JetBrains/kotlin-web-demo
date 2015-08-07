## All, Any and other predicates

Implement all the functions below using
[all](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/all.html),
[any](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/any.html),
[count](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/count.html),
[firstOrNull](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/find.html).

```kotlin
val numbers = listOf(-1, 0, 2)
val isZero: (Int) -> Boolean = { it == 0 }
numbers.any(isZero) == true
numbers.all(isZero) == false
numbers.count(isZero) == 1
numbers.firstOrNull { it > 0 } == 2
```