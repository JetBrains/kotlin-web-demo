import org.junit.Assert
import org.junit.Test
import koans.util.errorMessage

class TestIntroduction {
    @Test fun testSetOfCustomers(){
        Assert.assertTrue(errorMessage("getSetOfCustomers"),
                shop.getSetOfCustomers() == customers.values.toSet())
    }
}
