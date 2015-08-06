import org.junit.Assert
import org.junit.Test as test

class TestExtensionFunctionLiterals {
    test fun testIsOddAndIsEven() {
        Assert.assertEquals("The functions 'isOdd' and 'isEven' should be implemented correctly",
                listOf(false, true, true), task())

    }
}