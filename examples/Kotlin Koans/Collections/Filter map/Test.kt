import org.junit.Assert
import org.junit.Test as test

class TestFilterMap {
    test fun testCitiesCustomersAreFrom() {
        Assert.assertEquals(setOf(Canberra, Vancouver, Budapest, Ankara, Tokyo), shop.getCitiesCustomersAreFrom())
    }

    test fun testCustomersFromCity() {
        Assert.assertEquals(listOf(customers[lucas], customers[cooper]), shop.getCustomersFrom(Canberra))
    }
}
