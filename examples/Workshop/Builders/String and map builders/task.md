## String and map builders

Extension function literals are very useful for creating builders, e.g.:

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

Add and implement the function 'buildMap' with one parameter (of type extension function) creating a new HashMap,
building it and returning it as a result.
The usage of this function is shown below.