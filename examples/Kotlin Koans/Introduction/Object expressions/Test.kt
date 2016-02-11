import org.junit.Test
import org.junit.Assert
import koans.util.errorMessage

class TestObjectExpressions {
    @Test fun testSort() {
        Assert.assertEquals(errorMessage("getList"), listOf(5, 2, 1), getList())
    }
}
