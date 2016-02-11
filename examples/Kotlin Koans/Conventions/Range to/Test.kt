import org.junit.Assert
import org.junit.Test
import java.util.ArrayList
import koans.util.inEquals

class TestRangeTo {
    fun doTest(date: MyDate, first: MyDate, last: MyDate, shouldBeInRange: Boolean) {
        val message = "${date} should${if (shouldBeInRange) "" else "n't"} be in range: ${first}..${last}".inEquals()
        Assert.assertEquals(message, shouldBeInRange, checkInRange(date, first, last))
    }

    @Test fun testInRange() {
        doTest(MyDate(2014, 3, 22), MyDate(2014, 1, 1), MyDate(2015, 1, 1), shouldBeInRange = true)
    }
}