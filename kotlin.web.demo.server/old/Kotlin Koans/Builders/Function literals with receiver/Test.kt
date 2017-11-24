import org.junit.Assert
import org.junit.Test
import koans.util.inEquals

class TestExtensionFunctionLiterals {
    @Test fun testIsOddAndIsEven() {
        Assert.assertEquals("The functions 'isOdd' and 'isEven' should be implemented correctly".inEquals(),
                listOf(false, true, true), task())

    }
}