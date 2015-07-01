package i_introduction._8_Extension_Functions.StringExtensions

fun Int.r(): RationalNumber = throw Exception("Not implemented")
fun Pair<Int, Int>.r(): RationalNumber = throw Exception("Not implemented")

data class RationalNumber(val numerator: Int, val denominator: Int)