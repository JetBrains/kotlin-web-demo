##Defalut and named parameters
Functions in Kotlin can have
[default](http://kotlinlang.org/docs/reference/functions.html#default-arguments)
and
[named](http://kotlinlang.org/docs/reference/functions.html#named-arguments)
parameters.

For example, function [joinToString()](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/join-to-string.html) has 6
parameters, but all of them are default and this function can be called without arguments.

Using named parameters make function `toJSON()` produce JSON-style string (for example, "[1, 3, 5]")