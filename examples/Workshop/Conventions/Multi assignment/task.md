##Multi assignment
Kotlin [data classes](http://kotlinlang.org/docs/reference/data-classes.html) abstraction can be used if your class
do nothing but hold data.

Data class can be decomposed into it's properties by using
[multi-assignment](http://kotlinlang.org/docs/reference/multi-declarations.html#multi-declarations).

Using multi-assignment and function [withIndex](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/with-index.html)
implement function `addIndex` that sums each number in list with it's index.