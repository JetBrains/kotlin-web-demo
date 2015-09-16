<answer>fun Int.r(): RationalNumber = RationalNumber(this, 1)
fun Pair<Int, Int>.r(): RationalNumber = RationalNumber(this.component1(), this.component2())</answer>

data class RationalNumber(val numerator: Int, val denominator: Int)