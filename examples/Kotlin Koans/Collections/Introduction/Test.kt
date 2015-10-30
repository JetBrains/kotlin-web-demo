import org.junit.Assert
import org.junit.Test as test

class TestIntroduction {
    @test fun testSetOfCustomers(){
        Assert.assertEquals(shop.getSetOfCustomers(), customers.values.toSet())
    }
}
