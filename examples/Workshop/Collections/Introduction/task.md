##Introduction
This part of workshop was inspired by [GS Collections Kata](https://github.com/goldmansachs/gs-collections-kata).

For easy java compatibility we don't introduce our own collections, but use standard Java ones.
However there are two views on them: mutable and read-only.

```kotlin
fun useReadonlySet(set: Set<Int>) {
    // doesn't compile:
    //    set.add(1)
}
fun useMutableSet(set: MutableSet<Int>) {
    set.add(1)
}
```

There are a bunch of operations that help to transform a collection to another one, starting with 'to', for example:
[toSet](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-set.html),
[toList](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-list.html),
[toMap](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-map.html).

```kotlin
fun example(list: List<Int>): Set<Int> {
    list.toSet()
}
```

Your first task is to implement extension function `Shop.getSetOfCustomers()` using method `toSet()`. Class `Shop`
and all related classes can be found at [Shop.kt](http://localhost:8080/#/Workshop/Collections/Introduction/Shop.kt).