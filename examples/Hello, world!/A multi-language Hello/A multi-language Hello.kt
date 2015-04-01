/**
 * In this example, `val` denotes a declaration of a read-only local variable,
 * that is assigned an pattern matching expression.
 * See http://kotlinlang.org/docs/reference/control-flow.html#when-expression
 */

fun main(args: Array<String>) {
    val language = if (args.size() == 0) "EN" else args[0]
    println(when (language) {
        "EN" -> "Hello!"
        "FR" -> "Salut!"
        "IT" -> "Ciao!"
        else -> "Sorry, I can't greet you in $language yet"
    })
}