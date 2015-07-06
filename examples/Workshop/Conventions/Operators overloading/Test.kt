package ii_conventions

import org.junit.Assert
import org.junit.Test as test
import ii_conventions.TimeInterval.*

class _15_Operators_Overloading {
    test fun testAddTimeIntervals() {
        Assert.assertEquals(MyDate(2014, 5, 22), MyDate(1983, 5, 22).addTimeIntervals(YEAR, 31))
        Assert.assertEquals(MyDate(1983, 5, 29), MyDate(1983, 5, 22).addTimeIntervals(DAY, 7))
        Assert.assertEquals(MyDate(1983, 5, 29), MyDate(1983, 5, 22).addTimeIntervals(WEEK, 1))
    }

    test fun testAddOneTimeInterval() {
        Assert.assertEquals(MyDate(2015, 5, 9), MyDate(2014, 5, 1) + YEAR + WEEK + DAY)
    }
}
