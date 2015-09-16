## Named arguments

[Default and named](http://kotlinlang.org/docs/reference/functions.html#default-arguments)
arguments help to minimize the number of overloads and improve the readability of the function invocation.
The library function [`join`](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/join.html)
is declared with default values for parameters:

```
fun join(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    /* ... */
): String
```

It can be called on a collection of Strings.
Using named arguments make the function `joinOptions()` return the list in a JSON format (e.g., "[a, b, c]")