import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class TestFunctions() {
    @test fun collection() {
        Assert.assertEquals("[1, 2, 3, 42, 555]", toJSON(listOf(1, 2, 3, 42, 555)))
    }
}