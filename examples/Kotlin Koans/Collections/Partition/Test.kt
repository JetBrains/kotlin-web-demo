import org.junit.Assert
import org.junit.Test
import koans.util.errorMessage

class TestPartition {
    @Test fun testGetCustomersWhoHaveMoreUndeliveredOrdersThanDelivered() {
        Assert.assertTrue(errorMessage("getCustomerWithMaximumNumberOfOrders"),
                setOf(customers[reka]) == shop.getCustomersWithMoreUndeliveredOrdersThanDelivered())
    }
}
