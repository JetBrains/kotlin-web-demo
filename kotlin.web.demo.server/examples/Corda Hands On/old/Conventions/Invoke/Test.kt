import org.junit.Assert
import org.junit.Test
import koans.util.inEquals

class TestInvoke {
    @Test fun testInvokeTwice() = testInvokable(2, ::invokeTwice)

    private fun testInvokable(numberOfInvocations: Int, invokeSeveralTimes: (Invokable) -> Invokable) {
        val invokable = Invokable()
        val message = "The number of invocations is incorrect".inEquals()
        Assert.assertEquals(message, numberOfInvocations, invokeSeveralTimes(invokable).numberOfInvocations)
    }

    @Test fun testNumberOfInvocations() {
        testInvokable(1) { it() }
        testInvokable(5) { it()()()()() }
        testInvokable(0) { it }
    }
}
