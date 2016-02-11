import org.junit.Assert
import org.junit.Test
import koans.util.toMessage

class K_Compound_Tasks {

    @Test fun testMostExpensiveDeliveredProduct() {
        val testShop = shop("test shop for 'most expensive delivered product'",
                customer(lucas, Canberra,
                        order(isDelivered = false, products = idea),
                        order(reSharper)
                )
        )
        Assert.assertTrue("getMostExpensiveDeliveredProduct".toMessage(), reSharper == testShop.customers[0].getMostExpensiveDeliveredProduct())
    }

    @Test fun testNumberOfTimesEachProductWasOrdered() {
        Assert.assertTrue("getNumberOfTimesProductWasOrdered".toMessage(), 3 == shop.getNumberOfTimesProductWasOrdered(reSharper))
    }
}
