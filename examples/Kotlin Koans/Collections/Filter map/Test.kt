import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestFilterMap {
    @Test fun testCitiesCustomersAreFrom() {
        Assert.assertTrue("getCitiesCustomersAreFrom".toMessage(),
                setOf(Canberra, Vancouver, Budapest, Ankara, Tokyo) == shop.getCitiesCustomersAreFrom())
    }

    @Test fun testCustomersFromCity() {
        Assert.assertTrue("getCustomersFrom".toMessage(),
                listOf(customers[lucas], customers[cooper]) == shop.getCustomersFrom(Canberra))
    }
}
