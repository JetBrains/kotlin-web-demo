##Lazy property
Implement a [custom getter](http://kotlinlang.org/docs/reference/properties.html#getters-and-setters)
to make the `LazyProperty.lazy` val really lazy.
It should be initialized by `initializer()` invocation at the moment of the first access.

You can add as many additional properties as you need.

Do not use Delegates ;).