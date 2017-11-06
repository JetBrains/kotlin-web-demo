import org.junit.Test
import org.junit.Assert
import koans.util.toMessageInEquals

class TestFunctions() {
    @Test fun collection() {
        Assert.assertEquals("toJSON".toMessageInEquals(), "[1, 2, 3, 42, 555]", toJSON(listOf(1, 2, 3, 42, 555)))
    }
}