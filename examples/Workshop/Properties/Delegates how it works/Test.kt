import org.junit.Assert
import org.junit.Test as test

class TestDelegatesHowItWorks {
    test fun testDate() {
        val d = D()
        d.date = MyDate(2014, 1, 13)
        Assert.assertEquals(2014, d.date.year)
        Assert.assertEquals(1, d.date.month)
        Assert.assertEquals(13, d.date.dayOfMonth)
    }
}