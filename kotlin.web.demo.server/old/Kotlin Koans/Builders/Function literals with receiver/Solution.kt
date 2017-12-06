fun task(): List<Boolean> {

<answer>val isEven: Int.() -> Boolean = { this % 2 == 0 }
val isOdd: Int.() -> Boolean = { this % 2 != 0 }</answer>

    return listOf(42.isOdd(), 239.isOdd(), 294823098.isEven())
}