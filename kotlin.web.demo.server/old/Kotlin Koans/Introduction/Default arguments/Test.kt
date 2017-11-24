import org.junit.Test
import org.junit.Assert

class TestDefaultAndNamedParams() {

    @Test fun testDefaultAndNamedParams() {
        Assert.assertEquals(listOf("a42", "b1", "C42", "D2"), useFoo())
    }
}