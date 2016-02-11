import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestSum {
    @Test fun testGetTotalOrderPrice() {
        Assert.assertTrue("getTotalOrderPrice".toMessage(), customers[nathan]!!.getTotalOrderPrice() == 148.0)
    }
}
