## Named arguments

[Default and named](http://kotlinlang.org/docs/reference/functions.html#default-arguments)
arguments help to minimize the number of overloads and improve the readability of the function invocation.
The library function [`joinToString`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/kotlin.-iterable/join-to-string.html)
is declared with default values for parameters:

```
fun joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    /* ... */
): String
```

It can be called on a collection of Strings.
Specifying only two arguments make the function `joinOptions()` return the list in a JSON format (e.g., "[a, b, c]")