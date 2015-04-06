/**
 * There's some new syntax: you can say `val 'property name': 'Type' by 'expression'`.
 * The expression after by is the delegate, because get() and set() methods
 * corresponding to the property will be delegated to it.
 * Property delegates don't have to implement any interface, but they have
 * to provide methods named get() and set() to be called.</p>
 */

import Delegate
import Example

class Example {
    var p: String by Delegate()

    override fun toString() = "Example Class"
}

class Delegate() {
    fun get(thisRef: Any?, prop: PropertyMetadata): String {
        return "$thisRef, thank you for delegating '${prop.name}' to me!"
    }

    fun set(thisRef: Any?, prop: PropertyMetadata, value: String) {
        println("$value has been assigned to ${prop.name} in $thisRef")
    }
}

fun main(args: Array<String>) {
    val e = Example()
    println(e.p)
    e.p = "NEW"
}