/*
 * Your task is to implement a palindrome test.
 *
 * A string is called a palindrome when it reads the same way left-to-right
 * and right-to-left.
 *
 * See http://en.wikipedia.org/wiki/Palindrome
 */
package palindrome

import java.util.*

fun isPalindrome(s : String) : Boolean {
  // Write your solution here
  return "?"
}

fun main(args : Array<String>) {
  test(true,    "")
  test(true,    "a")
  test(true,    "aba")
  test(true,    "abba")
  test(true,    "abbabba")
  test(true,    "abbaabba")

  test(false,    "ab")
  test(false,    "abab")
  test(false,    "abaa")

  println("Success")
}

// HELPER FUNCTIONS

fun test(expected : Boolean, data : String) {
  val actual = isPalindrome(data)
  assertEquals(actual, expected, "\ndata = \"$data\"\n" +
                         "isPalindrome(data) = ${actual}, but must be $expected ")
}

fun assertEquals<T>(actual : T?, expected : T?, message : Any? = null) {
  if (actual != expected) {
    if (message == null)
      throw AssertionError()
    else
      throw AssertionError(message)
  }
}
