import org.junit.Assert
import org.junit.Test as test

class TestMaxMin {
    @test fun testCustomerWithMaximumNumberOfOrders() {
        Assert.assertEquals(customers[reka], shop.getCustomerWithMaximumNumberOfOrders())
    }

    @test fun testTheMostExpensiveOrderedProduct() {
        Assert.assertEquals(rubyMine, customers[nathan]!!.getMostExpensiveOrderedProduct())
    }
}
