##Delegates
[Delegated property](http://kotlinlang.org/docs/reference/delegated-properties.html#delegated-properties)
is  property which get and set method are delegated to some class.

Syntax for such kind of properties is `val/var <property name>: <Type> by <expression>`.

Property delegates don't have to implement any interface, but they have to provide a `get()` function
(and `set()` for var's).

Implement methods of the EffectiveDate class so it could be used as a delegate for properties
with type MyDate and delegated value would be stored as Long.

Use extension functions `MyDate.toMillis()` and `Long.toDate()`, defined at
[MyDate.kt](http://localhost:8080/#/Workshop/Properties/Delegates%20how%20it%20works/MyDate.kt)