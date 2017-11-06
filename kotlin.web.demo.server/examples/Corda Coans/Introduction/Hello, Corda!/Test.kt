/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import org.junit.Assert
import org.junit.Test

class TestStart {
    @Test fun testOk() {
        Assert.assertEquals("OK", start())
    }
}

package net.corda.training.state

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.finance.*
import net.corda.testing.*
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IOUStateTests {
    /**
     * Task 1.
     * TODO: Add an 'amount' property of type [Amount] to the [IOUState] class to get this test to pass.
     * Hint: [Amount] is a template class that takes a class parameter of the token you would like an [Amount] of.
     * As we are dealing with cash lent from one Party to another a sensible token to use would be [Currency].
     */
    @Test
    fun testHasIOUAmountFieldOfCorrectType() {
        // Does the amount field exist?
        IOUState::class.java.getDeclaredField("amount")
        // Is the amount field of the correct type?
        assertEquals(IOUState::class.java.getDeclaredField("amount").type, Amount::class.java)
    }
}