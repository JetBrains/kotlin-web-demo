import org.junit.Test
import org.junit.Assert

class TestLambdas() {
    @Test fun contains() {
        Assert.assertTrue("The result should be true if the collection contains an even number", containsEven(listOf(1, 2, 3, 126, 555)))
    }

    @Test fun notContains() {
        Assert.assertFalse("The result should be false if the collection doesn't contain an even number", containsEven(listOf(43, 33)))
    }
}