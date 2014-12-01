package sum
import org.junit.Test
import kotlin.test.*

fun my_fun(): Int {
    return 0
}

public class TestSum {


    Test fun testSum() {
        var i = 0
        System.out.println("Hello");
        assertEquals("Hello world!", "hello world!")
    }

    Test fun testSum2() {
        var i = 0
        assertEquals("Hello world!", "hello world!".capitalize())
    }

    Test fun testSum3() {
        var i: Int = 0 / 0
        assertEquals(i, 0)
    }
}