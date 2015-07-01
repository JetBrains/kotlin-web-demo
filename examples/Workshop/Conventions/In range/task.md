##In range
Kotlin operator `in` compiles to function `contains()`.

```kotlin
"a" in container
// compiles to
container.contains("a")
```

Implement function `DateRange.contains()` to make `in` operator work.

Note that DateRange become an implementation of standard
[Range interface](http://kotlinlang.org/docs/reference/ranges.html#common-interfaces-definition).