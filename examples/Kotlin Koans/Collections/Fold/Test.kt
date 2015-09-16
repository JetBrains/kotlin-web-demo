import org.junit.Assert
import org.junit.Test as test

class TestFold {
    @test fun testGetProductsOrderedByAllCustomers() {
        val testShop = shop("test shop for 'fold'",
                customer(lucas, Canberra,
                        order(idea),
                        order(webStorm)
                ),
                customer(reka, Budapest,
                        order(idea),
                        order(youTrack)
                )
        )
        Assert.assertEquals(setOf(idea), testShop.getProductsOrderedByAllCustomers())
    }
}
