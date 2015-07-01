##Properties
Classes in Kotlin can have [properties](http://kotlinlang.org/docs/reference/properties.html#properties-and-fields).
In the bytecode the property corresponds to field + getter + setter(if it's mutable).

The full syntax for declaring a property is

```
var <propertyName>: <PropertyType> [= <property_initializer>]
  <getter>
  <setter>
```

The initializer, getter and setter are optional.

Implement custom setter for `PropertyExample.propertyWithCounter` so that it will count the number of property
assignments.


