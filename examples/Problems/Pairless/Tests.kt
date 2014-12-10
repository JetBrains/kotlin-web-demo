package pairless

import java.util.*

class Tests {
    Test fun testPairless() {
        test(0, 0)
        test(1, 0, 1, 0)
        test(5, 5, -1, -1, 0, 0)
        test(3, 1, 3, 1, 1, 1)
        test(1, 2, 3, 1, 2, 3)

        println("Success")
    }
}

fun test(expected: Int?, vararg data: Int) {
    val actual = findPairless(data)
    assertEquals(expected, actual, "\ndata = ${Arrays.toString(data)}\n")
}