import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class TestSmartCasts {
    test fun testNum() {
        Assert.assertEquals("'eval' on Num should work:", 2, eval(Num(2)))
    }

    test fun testSum() {
        Assert.assertEquals("'eval' on Sum should work:", 3, eval(Sum(Num(2), Num(1))))
    }

    test fun testRecursion() {
        Assert.assertEquals("'eval' should work:", 6, eval(Sum(Sum(Num(1), Num(2)), Num(3))))
    }
}