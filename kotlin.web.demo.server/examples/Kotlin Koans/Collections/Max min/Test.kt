import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestMaxMin {
    @Test fun testCustomerWithMaximumNumberOfOrders() {
        Assert.assertEquals("getCustomerWithMaximumNumberOfOrders".toMessage(),
                customers[reka], shop.getCustomerWithMaximumNumberOfOrders())
    }

    @Test fun testTheMostExpensiveOrderedProduct() {
        Assert.assertEquals("getMostExpensiveOrderedProduct".toMessage(),
                rubyMine, customers[nathan]!!.getMostExpensiveOrderedProduct())
    }
}
