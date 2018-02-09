import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestFlatMap {
    @Test fun testGetOrderedProductsSet() {
        Assert.assertEquals("getOrderedProducts".toMessage(),
                setOf(idea), customers[reka]!!.orderedProducts)
    }

    @Test fun testGetAllOrderedProducts() {
        Assert.assertEquals("getAllOrderedProducts".toMessage(),
                orderedProducts, shop.allOrderedProducts)
    }
}
