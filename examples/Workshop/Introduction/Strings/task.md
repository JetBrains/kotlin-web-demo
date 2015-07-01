##Strings
Kotlin has two types of
[string literals](http://kotlinlang.org/docs/reference/basic-types.html#string-literals):
escaped strings that may have escaped characters in them and raw strings that can contain newlines and arbitrary text.

For example, this is two representations of a pattern that matches date in format `dd.mm.yyyy`:

```kotlin
fun getPatternInAUsualString() = "(\\d{2})\\.(\\d{2})\\.(\\d{4})"

fun getPatternInTQString() = """(\d{2})\.(\d{2})\.(\d{4})"""
```

Using [string templates](http://kotlinlang.org/docs/reference/basic-types.html#templates) and `month` variable
rewrite pattern in such a way that it matches date in format `13 JUN 1992`.