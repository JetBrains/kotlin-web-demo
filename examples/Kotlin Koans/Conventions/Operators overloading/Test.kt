import org.junit.Assert
import org.junit.Test as test
import TimeInterval.*

class TestOperatorsOverloading {
    @test fun testAddOneTimeInterval() {
        Assert.assertEquals(MyDate(2015, 5, 8), task1(MyDate(2014, 5, 1)))
    }

    @test fun testOneMonth() {
        Assert.assertEquals(MyDate(2016, 0, 27), task2(MyDate(2014, 0, 1)))
    }

    @test fun testMonthChange() {
        Assert.assertEquals(MyDate(2016, 1, 20), task2(MyDate(2014, 0, 25)))
    }
}
