##Object expressions
Sometimes we need to create an object of a slight modification of some class,
without explicitly declaring a new subclass for it. In Kotlin
[object expressions](http://kotlinlang.org/docs/reference/object-declarations.html#object-expressions)
can be used in such cases.

Add an object expression that extends MouseAdapter and counts the number of mouse clicks
as an argument to the function `handleMouse()`.