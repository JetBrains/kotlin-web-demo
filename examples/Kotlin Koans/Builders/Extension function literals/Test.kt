import org.junit.Assert
import org.junit.Test

class TestExtensionFunctionLiterals {
    @Test fun testIsOddAndIsEven() {
        Assert.assertEquals("The functions 'isOdd' and 'isEven' should be implemented correctly",
                listOf(false, true, true), task())

    }
}