## Strings

Read about different string literals and string templates in Kotlin
[here](http://kotlinlang.org/docs/reference/basic-types.html#string-literals).

Raw strings are useful for writing regex patterns, you don't need to escape a backslash by a backslash.
Below there is a pattern that matches a date in format `dd.mm.yyyy`;
in a usual string you'd have to write `"(\\d{2})\\.(\\d{2})\\.(\\d{4})"`.

```kotlin
fun getPattern() = """(\d{2})\.(\d{2})\.(\d{4})"""
```

Using `month` variable rewrite this pattern in such a way that it matches the date in format `13 JUN 1992`.