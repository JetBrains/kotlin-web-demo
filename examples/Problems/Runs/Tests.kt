/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package runs

import java.util.Arrays
import kotlin.test.*
import org.junit.Test
import java.util.*

class Tests {

    Test fun testRuns1() {
        test(0)
    }

    Test fun testRuns2() {
        test(1, 1)
    }

    Test fun testRuns3() {
        test(3, 1, 2, 3)
    }

    Test fun testRuns4() {
        test(3, 1, 2, 2, 3)
    }

    Test fun testRuns5() {
        test(1, 1, 1, 2, 3)
    }

    Test fun testRuns6() {
        test(1, 1, 1, 1, 1)
    }

    Test fun testRuns7() {
        test(3, 1, 1, 1, 0, 1, 1)
    }

    Test fun testRuns8() {
        test(3, 1, 1, 1, 0, 1)
    }

    Test fun testRuns9() {
        test(5, 1, 0, 1, 0, 1)
    }
}

fun test(expected: Int, vararg data: Int) {
    assertEquals(expected, runs(data), "\ndata = ${Arrays.toString(data)}")
}