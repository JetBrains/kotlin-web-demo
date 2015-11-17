<answer>fun Int.r(): RationalNumber = RationalNumber(this, 1)
fun Pair<Int, Int>.r(): RationalNumber = RationalNumber(first, second)</answer>

data class RationalNumber(val numerator: Int, val denominator: Int)