package i_introduction._6_Smart_Casts

fun print(e: Expr): String =
        when (e) {
            is Num -> "${e.value}"
            is Sum -> "${print(e.left)} + ${print(e.right)}"
            else -> throw IllegalArgumentException("Unknown expression")
        }

abstract class Expr
class Num(val value: Int) : Expr()
class Sum(val left: Expr, val right: Expr) : Expr()
