package palindrome

import org.junit.Test
import kotlin.test.*

class Tests {
    Test fun testEmptyString() {
        test(true, "")
    }

    Test fun testChar() {
        test(true, "a")
    }

    Test fun testPositive1() {
        test(true, "aba")
    }

    Test fun testPositive2() {
        test(true, "abba")
    }

    Test fun testPositive3() {
        test(true, "abbabba")
    }

    Test fun testPositive4() {
        test(true, "abbaabba")
    }

    Test fun testNegative1() {
        test(false, "ab")
    }

    Test fun testNegative2() {
        test(false, "abab")
    }

    Test fun testNegative3() {
        test(false, "abaa")
    }
}

fun test(expected: Boolean, data: String) {
    val actual = isPalindrome(data)
    assertEquals(expected, actual, "\ndata = \"$data\"")
}