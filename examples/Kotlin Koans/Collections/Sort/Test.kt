import org.junit.Assert
import org.junit.Test as test

class TestSort {
    @test fun testGetCustomersSortedByNumberOfOrders() {
        Assert.assertEquals(sortedCustomers, shop.getCustomersSortedByNumberOfOrders())
    }
}
