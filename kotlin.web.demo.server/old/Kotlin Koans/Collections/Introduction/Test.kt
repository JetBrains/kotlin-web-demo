import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class TestIntroduction {
    @Test fun testSetOfCustomers(){
        Assert.assertEquals("getSetOfCustomers".toMessage(),
                customers.values.toSet(), shop.getSetOfCustomers())
    }
}
