fun eval(expr: Expr): Int =
        when (expr) {
            is Num -> <answer>expr.value</answer>
            is Sum -> <answer>eval(expr.left) + eval(expr.right)</answer>
            else -> throw IllegalArgumentException("Unknown expression")
        }

interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr
