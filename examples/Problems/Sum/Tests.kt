package sum

import org.junit.Test
import kotlin.test.*
import java.util.*

public class Tests {
    Test fun testSum() {
        test(0)
        test(1, 1)
        test(-1, -1, 0)
        test(6, 1, 2, 3)
        test(6, 1, 1, 1, 1, 1, 1)
    }
}

fun test(expectedSum: Int, vararg data: Int) {
    val actualSum = sum(data)
    assertEquals(expectedSum, actualSum, "\ndata = ${Arrays.toString(data)}\n")
}