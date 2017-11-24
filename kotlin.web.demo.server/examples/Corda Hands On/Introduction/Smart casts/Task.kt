fun eval(expr: Expr): Int =
        when (expr) {
            is Num -> <taskWindow>TODO()</taskWindow>
            is Sum -> <taskWindow>TODO()</taskWindow>
            else -> throw IllegalArgumentException("Unknown expression")
        }

interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr
