import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class TestLambdas() {
    test fun contains() {
        Assert.assertTrue("The result should be true if the collection contains an even number", containsEven(listOf(1, 2, 3, 126, 555)))
    }

    test fun notContains() {
        Assert.assertFalse("The result should be false if the collection doesn't contain an even number", containsEven(listOf(43, 33)))
    }
}