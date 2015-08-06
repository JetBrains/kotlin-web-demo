import org.junit.Assert
import org.junit.Test as test

class TestComparison {

    test fun testBefore() {
        val first = MyDate(2014, 5, 10)
        val second = MyDate(2014, 7, 11)
        Assert.assertTrue("${first} should be before ${second}", first < second)
    }

    test fun testAfter() {
        val first = MyDate(2014, 10, 20)
        val second = MyDate(2014, 7, 11)
        Assert.assertTrue("${first} should be after ${second}", first > second)
    }
}