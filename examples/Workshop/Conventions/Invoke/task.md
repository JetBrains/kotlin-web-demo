##Invoke
Objects with `invoke()` method can be invoked as a function.

You can add invoke extension for any class, but it's better not to overdo it

```kotlin
fun Int.invoke() { println(this) }
```

Implement function `Invokable.invoke()` so it would count a number of invocations.