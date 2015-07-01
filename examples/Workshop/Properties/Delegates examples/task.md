##Delegates example
Kotlin standard library provides factory methods for several useful delegates like
[Delegates.lazy()](http://kotlinlang.org/docs/reference/delegated-properties.html#lazy),
[Delegates.observable()](http://kotlinlang.org/docs/reference/delegated-properties.html#observable),
[Delegates.notNull()](http://kotlinlang.org/docs/reference/delegated-properties.html#not-null),
[Delegates.mapVal()](http://kotlinlang.org/docs/reference/delegated-properties.html#storing-properties-in-a-map)

Using
[Delegates.mapVal()](http://kotlinlang.org/docs/reference/delegated-properties.html#storing-properties-in-a-map)
make the properties in class `Commodity` reflect the data stored in the map.
E.g., the value of property 'price' should be the value in 'data' by the key "price".