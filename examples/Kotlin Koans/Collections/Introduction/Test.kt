import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestIntroduction {
    @Test fun testSetOfCustomers(){
        Assert.assertTrue("getSetOfCustomers".toMessage(),
                shop.getSetOfCustomers() == customers.values.toSet())
    }
}
