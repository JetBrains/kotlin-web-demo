import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestMaxMin {
    @Test fun testCustomerWithMaximumNumberOfOrders() {
        Assert.assertTrue("getCustomerWithMaximumNumberOfOrders".toMessage(),
                customers[reka] == shop.getCustomerWithMaximumNumberOfOrders())
    }

    @Test fun testTheMostExpensiveOrderedProduct() {
        Assert.assertTrue("getMostExpensiveOrderedProduct".toMessage(),
                rubyMine == customers[nathan]!!.getMostExpensiveOrderedProduct())
    }
}
