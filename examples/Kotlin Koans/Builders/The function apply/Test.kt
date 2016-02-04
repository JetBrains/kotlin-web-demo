import org.junit.Assert
import org.junit.Test
import java.util.HashMap

class TestTheFunctionWith {
    @Test fun testCreateString() {
        val s = createString()
        val sb = StringBuilder()
        sb.append("Numbers: ")
        for (i in 1..10) {
            sb.append(i)
        }
        Assert.assertEquals("String should be built:", sb.toString(), s)
    }

    @Test fun testCreateMap() {
        val map = createMap()
        val expected = HashMap<Int, String>()
        for (i in 0..10) {
            expected[i] = "$i"
        }
        Assert.assertEquals("Map should be filled with the right values", expected, map)
    }
}
