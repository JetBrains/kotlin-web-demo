##Comparison
Classes which inherit from interface [Comparable](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-comparable/index.html)
have a defined total ordering between their instances.

Function `Comparable.compareTo()` compares this object with the specified object for order.
Returns zero if this object is equal to the specified other object, a negative number if its less than other,
or a positive number if its greater than other.

```kotlin
b1 < b2
// is compiled to
b1.compareTo(b2) < 0

b1 >= b2
// is compiled to
b1.compareTo(b2) >= 0
```

Implement function `MyDate.compareTo()` to make class MyDate comparable.

