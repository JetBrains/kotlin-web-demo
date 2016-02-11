import org.junit.Test
import org.junit.Assert
import koans.util.errorMessage

class TestNamedArguments() {

    @Test fun testJoinToString() {
        Assert.assertEquals(errorMessage("joinOptions"), "[yes, no, may be]", joinOptions(listOf("yes", "no", "may be")))
    }

}