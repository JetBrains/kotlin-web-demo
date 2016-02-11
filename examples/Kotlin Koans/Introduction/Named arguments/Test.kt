import org.junit.Test
import org.junit.Assert
import koans.util.toMessageInEquals

class TestNamedArguments() {

    @Test fun testJoinToString() {
        Assert.assertEquals("joinOptions".toMessageInEquals(), "[yes, no, may be]", joinOptions(listOf("yes", "no", "may be")))
    }

}