package i_introduction._6_Smart_Casts

fun print(e: Expr): String = throw Exception("Not implemented")

abstract class Expr
class Num(val value: Int) : Expr()
class Sum(val left: Expr, val right: Expr) : Expr()
