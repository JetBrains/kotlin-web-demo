package v_collections

import org.junit.Assert
import org.junit.Test as test
import v_collections.data.*

class C_All_Any_And_Other_Predicates {

    test fun testAllCustomersAreFromCity() {
        Assert.assertFalse(shop.checkAllCustomersAreFrom(Canberra))
    }

    test fun testAnyCustomerIsFromCity() {
        Assert.assertTrue(shop.hasCustomerFrom(Canberra))
    }

    test fun testCountCustomersFromCity() {
        Assert.assertEquals(2, shop.countCustomersFrom(Canberra))
    }

    test fun testAnyCustomerFromCity() {
        Assert.assertEquals(customers[lucas], shop.findAnyCustomerFrom(Canberra))
    }
}
