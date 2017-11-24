import org.junit.Assert
import org.junit.Test
import koans.util.toMessageInEquals

class TestOperatorsOverloading {
    @Test fun testAddOneTimeInterval() {
        Assert.assertEquals("task1".toMessageInEquals(), MyDate(2015, 5, 8), task1(MyDate(2014, 5, 1)))
    }

    @Test fun testOneMonth() {
        Assert.assertEquals("task2".toMessageInEquals(), MyDate(2016, 0, 27), task2(MyDate(2014, 0, 1)))
    }

    @Test fun testMonthChange() {
        Assert.assertEquals("task2".toMessageInEquals(), MyDate(2016, 1, 20), task2(MyDate(2014, 0, 25)))
    }
}
