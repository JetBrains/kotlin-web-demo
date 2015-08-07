import org.junit.Test as test
import org.junit.Assert

class TestObjectExpressions {
    test fun testSort() {
        Assert.assertEquals(listOf(5, 2, 1), getList())
    }
}
