import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestAllAnyAndOtherPredicates {

    @Test fun testAllCustomersAreFromCity() {
        Assert.assertFalse("checkAllCustomersAreFrom".toMessage(), shop.checkAllCustomersAreFrom(Canberra))
    }

    @Test fun testAnyCustomerIsFromCity() {
        Assert.assertTrue("hasCustomerFrom".toMessage(), shop.hasCustomerFrom(Canberra))
    }

    @Test fun testCountCustomersFromCity() {
        Assert.assertTrue("countCustomersFrom".toMessage(), 2 == shop.countCustomersFrom(Canberra))
    }

    @Test fun testAnyCustomerFromCity() {
        Assert.assertTrue("findAnyCustomerFrom".toMessage(), customers[lucas] == shop.findAnyCustomerFrom(Canberra))
    }
}
