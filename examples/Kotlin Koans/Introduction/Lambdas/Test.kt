import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class TestLambdas() {
    test fun contains() {
        Assert.assertTrue(containsEven(listOf(1, 2, 3, 126, 555)))
    }

    test fun notContains() {
        Assert.assertFalse(containsEven(listOf(43, 33)))
    }
}