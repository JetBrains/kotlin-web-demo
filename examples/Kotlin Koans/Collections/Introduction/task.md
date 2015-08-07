## Introduction

This part was inspired by [GS Collections Kata](https://github.com/goldmansachs/gs-collections-kata).

For easy java compatibility we don't introduce our own collections, but use standard Java ones.
Read about [read-only and mutable views on Java collections](http://blog.jetbrains.com/kotlin/2012/09/kotlin-m3-is-out/#Collections).

In Kotlin standard library there are lots of extension functions that make the work with collections more convenient.
For example, operations that transform a collection to another one, starting with 'to':
[toSet](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-set.html) or
[toList](http://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to-list.html).

Implement an extension function `Shop.getSetOfCustomers()`.
The class `Shop` and all related classes can be found at [Shop.kt](/#/Kotlin%20Koans/Collections/Introduction/Shop.kt).