package iv_builders

import junit.framework.Assert
import org.junit.Test as test
import java.util.HashMap

class _22_Extension_Function_Literals {
    test fun testIsOddAndIsEven() {
        Assert.assertEquals("The functions 'isOdd' and 'isEven' should be implemented correctly",
                listOf(false, true, true), listOf(42.isOdd(), 239.isOdd(), 294823098.isEven()))

    }
}