import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class TestNamedArguments() {

    @test fun testJoinToString() {
        Assert.assertEquals("[yes, no, may be]", joinOptions(listOf("yes", "no", "may be")))
    }

}