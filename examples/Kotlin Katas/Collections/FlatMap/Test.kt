import org.junit.Assert
import org.junit.Test as test

class TestFlatMap {
    @test fun testGetOrderedProductsSet() {
        Assert.assertEquals(setOf(idea), customers[reka]!!.orderedProducts)
    }

    @test fun testGetAllOrderedProducts() {
        Assert.assertEquals(orderedProducts, shop.allOrderedProducts)
    }
}
