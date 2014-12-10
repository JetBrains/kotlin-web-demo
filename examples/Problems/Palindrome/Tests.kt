package palindrome

class Tests {
    Test fun testPalindrome() {
        test(true, "")
        test(true, "a")
        test(true, "aba")
        test(true, "abba")
        test(true, "abbabba")
        test(true, "abbaabba")

        test(false, "ab")
        test(false, "abab")
        test(false, "abaa")
    }
}

fun test(expected: Boolean, data: String) {
    val actual = isPalindrome(data)
    assertEquals(expected, actual, "\ndata = \"$data\"\n")
}