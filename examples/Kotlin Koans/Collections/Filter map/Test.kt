import org.junit.Assert
import org.junit.Test

class TestFilterMap {
    @Test fun testCitiesCustomersAreFrom() {
        Assert.assertEquals(setOf(Canberra, Vancouver, Budapest, Ankara, Tokyo), shop.getCitiesCustomersAreFrom())
    }

    @Test fun testCustomersFromCity() {
        Assert.assertEquals(listOf(customers[lucas], customers[cooper]), shop.getCustomersFrom(Canberra))
    }
}
