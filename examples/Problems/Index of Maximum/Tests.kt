package maxindex

import org.junit.Test
import kotlin.test.*
import java.util.Arrays

class Tests {
    Test fun testIndexOfMaximum() {
        test(null)
        test(0, 0)
        test(1, -1, 0)
        test(0, -1, -2)
        test(1, 1, 3, 2, 1)
        test(2, 1, 3, 4, 1)
        test(2, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
        test(2, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE)
    }
}

fun test(expected: Int?, vararg data: Int) {
    val actual = indexOfMax(data)
    assertEquals(expected, actual, "\ndata = ${Arrays.toString(data)}\n")
}
