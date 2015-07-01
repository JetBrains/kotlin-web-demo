##Lambdas
Kotlin supports a lot of functional programming things like
[higher-order functions](http://kotlinlang.org/docs/reference/lambdas.html#higher-order-functions)
and
[function literals](http://kotlinlang.org/docs/reference/lambdas.html#function-literals-and-function-expressions).

For example, function [collection.any()](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/any.html)
is a higher-order function, i.e. it takes a function value as an argument. This argument is an
expression that is itself a function, i.e. a function literal.

Use this function to check if collection contains an even number.