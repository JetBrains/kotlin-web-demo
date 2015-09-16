import org.junit.Test as test
import org.junit.Assert

class TestDefaultAndNamedParams() {

    @test fun testDefaultAndNamedParams() {
        Assert.assertEquals(listOf("a42", "b1", "C42", "D2"), useFoo())
    }
}