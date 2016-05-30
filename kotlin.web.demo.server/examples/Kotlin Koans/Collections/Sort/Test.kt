import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestSort {
    @Test fun testGetCustomersSortedByNumberOfOrders() {
        Assert.assertTrue("getCustomersSortedByNumberOfOrders".toMessage(), sortedCustomers == shop.getCustomersSortedByNumberOfOrders())
    }
}
