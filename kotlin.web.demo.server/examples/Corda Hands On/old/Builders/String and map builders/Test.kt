import org.junit.Assert
import org.junit.Test
import koans.util.inEquals

class TestStringAndMapBuilders {
    @Test fun testBuildMap() {
        val map: Map<Int, String> = buildMap {
            put(0, "0")
            for (i in 1..10) {
                put(i, "$i")
            }
        }
        val expected = hashMapOf<Int, String>()
        for (i in 0..10) {
            expected[i] = "$i"
        }
        Assert.assertEquals("Map should be filled with the right values".inEquals(), expected, map)
    }
}