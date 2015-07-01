package i_introduction._3_Lambdas

import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class _03_Lambdas() {
    test fun contains() {
        Assert.assertTrue(containsEven(listOf(1, 2, 3, 126, 555)))
    }

    test fun notContains() {
        Assert.assertFalse(containsEven(listOf(43, 33)))
    }
}