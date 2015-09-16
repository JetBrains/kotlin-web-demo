## Extension functions on collections

Kotlin code can be easily mixed with Java code.
Thus in Kotlin we don't introduce our own collections, but use standard Java ones (slightly improved).
Read about [read-only and mutable views on Java collections](http://blog.jetbrains.com/kotlin/2012/09/kotlin-m3-is-out/#Collections).

In [Kotlin standard library](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/)
there are lots of extension functions that make the work with collections more convenient.
Rewrite the previous example once more using an extension function
[sortDescending](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/sort-descending.html).