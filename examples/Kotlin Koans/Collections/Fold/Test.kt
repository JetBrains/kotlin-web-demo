import org.junit.Assert
import org.junit.Test
import koans.util.errorMessage

class TestFold {
    @Test fun testGetProductsOrderedByAllCustomers() {
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
        Assert.assertTrue(errorMessage("getSetOfProductsOrderedByEveryCustomer"),
                setOf(idea) == testShop.getSetOfProductsOrderedByEveryCustomer())
    }
}
