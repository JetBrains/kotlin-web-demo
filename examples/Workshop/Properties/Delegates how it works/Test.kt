package iii_properties

import org.junit.Assert
import org.junit.Test as test

class D {
    var date by EffectiveDate()
}

class _21_Delegates_How_It_Works {
    test fun testDate() {
        val d = D()
        d.date = MyDate(2014, 1, 13)
        Assert.assertEquals(2014, d.date.year)
        Assert.assertEquals(1, d.date.month)
        Assert.assertEquals(13, d.date.dayOfMonth)
    }
}