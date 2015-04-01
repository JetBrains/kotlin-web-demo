/**
 * `if` is an expression, i.e. it returns a value.
 * Therefore there is no ternary operator (condition ? then : else),
 * because ordinary `if` works fine in this role.
 * See http://kotlinlang.org/docs/reference/control-flow.html#if-expression
 */
fun main(args: Array<String>) {
    println(max(Integer.parseInt(args[0]), Integer.parseInt(args[1])))
}

fun max(a: Int, b: Int) = if (a > b) a else b