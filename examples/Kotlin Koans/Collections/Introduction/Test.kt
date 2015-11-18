import org.junit.Assert
import org.junit.Test

class TestIntroduction {
    @Test fun testSetOfCustomers(){
        Assert.assertEquals(shop.getSetOfCustomers(), customers.values.toSet())
    }
}
