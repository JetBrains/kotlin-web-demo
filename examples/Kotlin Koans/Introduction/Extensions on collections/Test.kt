import org.junit.Test as test
import org.junit.Assert

class TestExtensionsOnCollections {
    test fun testSort() {
        Assert.assertEquals(listOf(5, 2, 1), getList())
    }
}