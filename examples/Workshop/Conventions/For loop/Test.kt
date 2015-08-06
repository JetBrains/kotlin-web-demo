import org.junit.Assert
import org.junit.Test as test

class TestForLoop {
    test fun testIterateOverDateRange() {
        val actualDateRange = arrayListOf<MyDate>()
        iterateOverDateRange(MyDate(2016, 5, 1), MyDate(2016, 5, 5), {
            date-> actualDateRange.add(date)
        })
        val expectedDateRange = arrayListOf(
                MyDate(2016, 5, 1), MyDate(2016, 5, 2), MyDate(2016, 5, 3), MyDate(2016, 5, 4), MyDate(2016, 5, 5))
        Assert.assertEquals("Incorrect iteration over five nice spring dates",
                expectedDateRange, actualDateRange)
    }

    test fun testIterateOverEmptyRange() {
        var invoked = false
        iterateOverDateRange(MyDate(2016, 1, 1), MyDate(2015, 1, 1), { invoked = true })
        Assert.assertFalse("Handler was invoked on an empty range", invoked)
    }
}