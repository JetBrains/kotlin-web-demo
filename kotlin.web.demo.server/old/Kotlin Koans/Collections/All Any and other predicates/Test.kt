import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestAllAnyAndOtherPredicates {

    @Test fun testAllCustomersAreFromCity() {
        Assert.assertFalse("checkAllCustomersAreFrom".toMessage(),
                shop.checkAllCustomersAreFrom(Canberra))
    }

    @Test fun testAnyCustomerIsFromCity() {
        Assert.assertTrue("hasCustomerFrom".toMessage(), shop.hasCustomerFrom(Canberra))
    }

    @Test fun testCountCustomersFromCity() {
        Assert.assertEquals("countCustomersFrom".toMessage(), 2, shop.countCustomersFrom(Canberra))
    }

    @Test fun testAnyCustomerFromCity() {
        Assert.assertEquals("findAnyCustomerFrom".toMessage(), customers[lucas], shop.findAnyCustomerFrom(Canberra))
        Assert.assertEquals("findAnyCustomerFrom".toMessage(), null, shop.findAnyCustomerFrom(City("Chicago")))
    }
}
