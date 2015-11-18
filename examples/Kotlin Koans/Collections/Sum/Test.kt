import org.junit.Assert
import org.junit.Test

class TestSum {
    @Test fun testGetTotalOrderPrice() {
        Assert.assertEquals(148.0, customers[nathan]!!.getTotalOrderPrice(), 0.1)
    }
}
