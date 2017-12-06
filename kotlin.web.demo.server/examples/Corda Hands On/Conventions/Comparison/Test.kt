import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestComparison {

    @Test fun testBefore() {
        val first = MyDate(2014, 5, 10)
        val second = MyDate(2014, 7, 11)
        Assert.assertTrue("compareTo".toMessage() + ": ${first} should go before ${second}", first < second)
    }

    @Test fun testAfter() {
        val first = MyDate(2014, 10, 20)
        val second = MyDate(2014, 7, 11)
        Assert.assertTrue("compareTo".toMessage() + ": ${first} should go after ${second}", first > second)
    }
}