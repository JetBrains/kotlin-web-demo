package kotlin

import js.native

/** Provides [] access to maps */
native("put")
public fun <K, V> Map<K, V>.set(key : K, value : V): Unit {
    this.put(key, value)
}
