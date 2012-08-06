/*
 * Think of a perfect world where everybody has a soulmate.
 * Now, the real world is imperfect: there is exactly one number in the array
 * that does not have a pair. A pair is an element with the same value.
 * For example in this array:
 *   1, 2, 1, 2
 * every number has a pair, but in this one:
 *   1, 1, 1
 * one of the ones is lonely.
 *
 * Your task is to implement the findPairless() function so that it finds the
 * lonely number and returns it.
 *
 * A hint: there's a solution that looks at each element only once and uses no
 * data structures like collections or trees.
 */
package pairless

import java.util.*

fun findPairless(a : IntArray) : Int {
  // Write your solution here
  return "?"
}

fun main(args : Array<String>) {
  test(0,    0)
  test(1,    0, 1, 0)
  test(5,    5, -1, -1, 0, 0)
  test(3,    1, 3, 1, 1, 1)
  test(1,    2, 3, 1, 2, 3)

  println("Success")
}

// HELPER FUNCTIONS

fun test(expected : Int?, vararg data : Int) {
  val actual = findPairless(data)
  assertEquals(actual, expected, "\ndata = ${Arrays.toString(data)}\n" +
                         "pairless(data) = ${actual}, but must be $expected ")
}

fun assertEquals<T>(actual : T?, expected : T?, message : Any? = null) {
  if (actual != expected) {
    if (message == null)
      throw AssertionError()
    else
      throw AssertionError(message)
  }
}
