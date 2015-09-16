## Delegates example
Read about [delegated properties](http://kotlinlang.org/docs/reference/delegated-properties.html)
(note how to declare a lazy val).

Using
[`Delegates.mapVal()`](http://kotlinlang.org/docs/reference/delegated-properties.html#storing-properties-in-a-map)
make the properties in class `Commodity` reflect the data stored in the map.
E.g., the value of property 'price' should be the value in 'data' by the key "price".