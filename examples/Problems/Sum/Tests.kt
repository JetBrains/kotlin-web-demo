package sum

import org.junit.Test
import kotlin.test.*
import java.util.*

public class Tests {
    Test fun testSum1() {
        test(0)
    }

    Test fun testSum2() {
        test(1, 1)
    }

    Test fun testSum3() {
        test(-1, -1, 0)
    }

    Test fun testSum4() {
        test(6, 1, 2, 3)
    }

    Test fun testSum5() {
        test(6, 1, 1, 1, 1, 1, 1)
    }
}

fun test(expectedSum: Int, vararg data: Int) {
    assertEquals(expectedSum, sum(data), "\ndata = ${Arrays.toString(data)}")
}