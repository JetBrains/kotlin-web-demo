## Named arguments

There is a library function [join](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/join.html)
that can be called on a collection of Strings.
It has five parameters, but for most of the cases the default values for these parameters can be used:

```
fun join(separator: String = ", ", prefix: String = "", postfix: String = "", ...): String
```

Using named arguments make the function `joinOptions()` return the list in a JSON format (e.g., "[a, b, c]")