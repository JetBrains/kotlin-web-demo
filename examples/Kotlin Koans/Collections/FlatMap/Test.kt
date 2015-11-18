import org.junit.Assert
import org.junit.Test

class TestFlatMap {
    @Test fun testGetOrderedProductsSet() {
        Assert.assertEquals(setOf(idea), customers[reka]!!.getOrderedProducts())
    }

    @Test fun testGetAllOrderedProducts() {
        Assert.assertEquals(orderedProducts, shop.getAllOrderedProducts())
    }
}
