import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestFlatMap {
    @Test fun testGetOrderedProductsSet() {
        Assert.assertTrue("getOrderedProducts".toMessage(),
                setOf(idea) == customers[reka]!!.getOrderedProducts())
    }

    @Test fun testGetAllOrderedProducts() {
        Assert.assertTrue("getAllOrderedProducts".toMessage(),
                orderedProducts == shop.getAllOrderedProducts())
    }
}
