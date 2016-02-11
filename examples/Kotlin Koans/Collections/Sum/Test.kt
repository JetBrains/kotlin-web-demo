import org.junit.Assert
import org.junit.Test
import koans.util.errorMessage

class TestSum {
    @Test fun testGetTotalOrderPrice() {
        Assert.assertTrue(errorMessage("getTotalOrderPrice"), customers[nathan]!!.getTotalOrderPrice() == 148.0)
    }
}
