import org.junit.Assert
import org.junit.Test
import koans.util.errorMessage

class TestMaxMin {
    @Test fun testCustomerWithMaximumNumberOfOrders() {
        Assert.assertTrue(errorMessage("getCustomerWithMaximumNumberOfOrders"),
                customers[reka] == shop.getCustomerWithMaximumNumberOfOrders())
    }

    @Test fun testTheMostExpensiveOrderedProduct() {
        Assert.assertTrue(errorMessage("getMostExpensiveOrderedProduct"),
                rubyMine == customers[nathan]!!.getMostExpensiveOrderedProduct())
    }
}
