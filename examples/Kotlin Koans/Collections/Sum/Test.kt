import org.junit.Assert
import org.junit.Test as test

class TestSum {
    test fun testGetTotalOrderPrice() {
        Assert.assertEquals(148.0, customers[nathan]!!.getTotalOrderPrice(), 0.1)
    }
}
