##String and map builders
[Extension function literals](http://kotlinlang.org/docs/reference/lambdas.html#extension-function-expressions)
that were introduced in previous task can be very useful for creating builders of different kind. For example:

```kotlin
fun buildString(build: StringBuilder.() -> Unit): String {
    val stringBuilder = StringBuilder()
    stringBuilder.build()
    return stringBuilder.toString()
}

val s = buildString {
    this.append("Numbers: ")
    for (i in 1..3) {
        // 'this' can be omitted
        append(i)
    }
}

s == "Numbers: 123"
```

Implement `buildMap()` so it would be the same type builder for map.

Use function `hashMapOf()` to create a map.