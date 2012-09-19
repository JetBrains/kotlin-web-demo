/*
 * Any array may be viewed as a number of "runs" of equal numbers.
 * For example, the following array has two runs:
 *   1, 1, 1, 2, 2
 * Three 1's in a row for the first run, and two 2's form the second.
 * This array has two runs of length one:
 *   3, 4
 * And this one has five runs:
 *   1, 0, 1, 1, 1, 2, 0, 0, 0, 0, 0, 0, 0
 * Your task is to implement the runs() function so that it returns the number
 * of runs in the given array.
 */
package runs

import java.util.*

fun runs(a : IntArray) : Int {
  // Write your solution here
  return "?"
}

fun main(args : Array<String>) {
  tests<Int> {
    on () expect 0
    on (1) expect 1
    on (1, 2, 3) expect 3
    on (1, 2, 2, 3) expect 3
    on (1, 2, 3, 3) expect 3
    on (1, 1, 2, 3) expect 3
    on (1, 1, 1) expect 1
    on (1, 1, 1, 0, 1, 1) expect 3
    on (1, 1, 1, 0, 1) expect 3
    on (1, 0, 1, 0, 1) expect 5
  } forFunction {
    runs(it)
  }
}






// HELPER FUNCTIONS

fun tests<R>(tests : Tester<R>.() -> Unit) : TesterRunner<R> {
  return TesterRunner<R>(tests)
}

class TesterRunner<R>(val init : Tester<R>.() -> Unit) {
  fun forFunction(functionUnderTest : (IntArray) -> R) {
    val tester = Tester(functionUnderTest)
    tester.(init)()
    tester.done()
  }
}

class Tester<R>(val f : (IntArray) -> R, val skipSuccessful : Boolean = true) {
  private var errors = 0
  fun done() {
      if (errors != 0 || !skipSuccessful)
        println("============================")
      if (errors > 0) {
        val suf = if (errors > 1) "s" else ""
        println("FAILURE: $errors test$suf failed")
      }
      else
        println("All tests passed")
  }

  fun on(vararg data : Int) : IntArray = data

  fun IntArray.expect(expected : R) {
    if (!skipSuccessful) {
      print("Run on: ${Arrays.toString(this)}...")
    }
    val result = (f)(this)
    assertEquals(expected, result, "  data     = ${Arrays.toString(this)}\n" +
                                   "  result   = $result\n" +
                                   "  expected = $expected\n")
  }

  private fun assertEquals<T>(actual : T?, expected : T?, message : Any? = null) {
    if (actual != expected) {
      errors++
      println("Test failed")
      val trace = Thread.currentThread()?.getStackTrace()!!
      if (trace.size > 6) {
        // Finding relevant stack frames
        val location = trace.getFrameAfter("runs.Tester", "expect")
        val function = trace.getFrameAfter("runs.TesterRunner", "forFunction")
        println("at ${function?.getClassName()}.${function?.getMethodName()}(line:${location?.getLineNumber()})")
      }
      if (message != null)
        println(message)
    }
    else if (!skipSuccessful)
      println("OK")
  }
}

fun Array<StackTraceElement?>.indexOf(className : String?, methodName : String?) : Int? {
  for (i in indices) {
    val frame = this[i]!!
    if (frame.getClassName() == className && frame.getMethodName() == methodName)
      return i
  }
  return null
}

fun Array<StackTraceElement?>.getFrameAfter(className : String?, methodName : String?) : StackTraceElement? {
  val index = indexOf(className, methodName)
  if (index == null) return null
  return this[index + 1]
}
