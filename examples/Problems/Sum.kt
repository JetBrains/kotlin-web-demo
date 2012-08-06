/*
 * Your task is to implement the sum() function so that it computes a sum of
 * all elements in the given array a.
 */
package sum

import java.util.*

fun sum(a : IntArray) : Int {
  // Write your solution here
  return "?"
}

fun main(args : Array<String>) {
  test(0)
  test(1, 1)
  test(-1, -1, 0)
  test(6, 1, 2, 3)
  test(6, 1, 1, 1, 1, 1, 1)

  println("Success")
}









// HELPER FUNCTIONS

fun test(expectedSum : Int, vararg data : Int) {
  val actualSum = sum(data)
  assertEquals(actualSum, expectedSum, "\ndata = ${Arrays.toString(data)}\n" +
                         "sum(data) = ${actualSum}, but must be $expectedSum ")
}

fun assertEquals<T>(actual : T?, expected : T?, message : Any? = null) {
  if (actual != expected) {
    if (message == null)
      throw AssertionError()
    else
      throw AssertionError(message)
  }
}
