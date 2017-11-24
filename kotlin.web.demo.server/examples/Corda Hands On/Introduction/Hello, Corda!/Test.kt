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

package net.corda.training.state

import org.junit.Assert
import net.corda.core.contracts.*
import net.corda.core.identity.Party
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestStart {
    @Test
    fun testHasIOUAmountFieldOfCorrectType() {
        // Does the amount field exist?
        IOUState::class.java.getDeclaredField("amount")
        // Is the amount field of the correct type?
        assertEquals(IOUState::class.java.getDeclaredField("amount").type, Amount::class.java)
    }

    @Test
    fun hasLenderFieldOfCorrectType() {
        // Does the lender field exist?
        IOUState::class.java.getDeclaredField("lender")
        // Is the lender field of the correct type?
        assertEquals(IOUState::class.java.getDeclaredField("lender").type, Party::class.java)
    }

    @Test
    fun hasBorrowerFieldOfCorrectType() {
        // Does the borrower field exist?
        IOUState::class.java.getDeclaredField("borrower")
        // Is the borrower field of the correct type?
        assertEquals(IOUState::class.java.getDeclaredField("borrower").type, Party::class.java)
    }

}

