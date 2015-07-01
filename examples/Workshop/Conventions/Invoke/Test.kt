package ii_conventions

import junit.framework.Assert
import org.junit.Test as test

class _17_Invoke {
    test fun testInvokeTwice() = testInvokable(2, ::invokeTwice)

    private fun testInvokable(numberOfInvocations: Int, invokeSeveralTimes: (Invokable) -> Invokable) {
        val invokable = Invokable()
        val message = "The number of invocations is incorrect"
        Assert.assertEquals(message, numberOfInvocations, invokeSeveralTimes(invokable).numberOfInvocations)
    }

    test fun testNumberOfInvocations() {
        testInvokable(1) { it() }
        testInvokable(5) { it()()()()() }
        testInvokable(0) { it }
    }
}
