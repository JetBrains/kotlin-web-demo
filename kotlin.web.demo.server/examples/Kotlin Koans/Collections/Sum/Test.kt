import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestSum {
    @Test fun testGetTotalOrderPrice() {
        Assert.assertEquals("getTotalOrderPrice".toMessage(),
                148.0, customers[nathan]!!.getTotalOrderPrice(), 0.001)
    }

    @Test fun testTotalPriceForRepeatedProducts() {
        Assert.assertEquals("getTotalOrderPrice".toMessage(),
                586.0, customers[lucas]!!.getTotalOrderPrice(), 0.001)
    }
}
