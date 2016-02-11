import org.junit.Test
import org.junit.Assert
import koans.util.toMessageInEquals

class TestSamConversions {
    @Test fun testSort() {
        Assert.assertEquals("getList".toMessageInEquals(), listOf(5, 2, 1), getList())
    }
}
