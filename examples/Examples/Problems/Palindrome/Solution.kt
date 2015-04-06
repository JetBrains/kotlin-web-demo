package palindrome

fun isPalindrome(s: String): Boolean {
    return s.equals(s.reverse())
}