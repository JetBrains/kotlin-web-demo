import org.junit.Assert
import org.junit.Test
import koans.util.errorMessage

class K_Compound_Tasks {

    @Test fun testMostExpensiveDeliveredProduct() {
        val testShop = shop("test shop for 'most expensive delivered product'",
                customer(lucas, Canberra,
                        order(isDelivered = false, products = idea),
                        order(reSharper)
                )
        )
        Assert.assertTrue(errorMessage("getMostExpensiveDeliveredProduct"), reSharper == testShop.customers[0].getMostExpensiveDeliveredProduct())
    }

    @Test fun testNumberOfTimesEachProductWasOrdered() {
        Assert.assertTrue(errorMessage("getNumberOfTimesProductWasOrdered"), 3 == shop.getNumberOfTimesProductWasOrdered(reSharper))
    }
}
