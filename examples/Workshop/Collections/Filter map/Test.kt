package v_collections

import org.junit.Assert
import org.junit.Test as test
import v_collections.data.*

class B_Filter_Map {
    test fun testCitiesCustomersAreFrom() {
        Assert.assertEquals(setOf(Canberra, Vancouver, Budapest, Ankara, Tokyo), shop.getCitiesCustomersAreFrom())
    }

    test fun testCustomersFromCity() {
        Assert.assertEquals(listOf(customers[lucas], customers[cooper]), shop.getCustomersFrom(Canberra))
    }
}
