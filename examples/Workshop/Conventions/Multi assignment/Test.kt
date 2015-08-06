import org.junit.Assert
import org.junit.Test as test

class TestMultiAssignment {
    test fun testIsLeapDay() {
        Assert.assertTrue(isLeapDay(MyDate(2016, 2, 29)))
        Assert.assertFalse(isLeapDay(MyDate(2015, 2, 29)))
    }
}