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
        Assert.assertEquals("getMostExpensiveDeliveredProduct".toMessage(), reSharper, testShop.customers[0].getMostExpensiveDeliveredProduct())
    }

    @Test fun testNumberOfTimesEachProductWasOrdered() {
        Assert.assertEquals(4, shop.getNumberOfTimesProductWasOrdered(idea))
    }

    @Test fun testNumberOfTimesEachProductWasOrderedForRepeatedProduct() {
        Assert.assertEquals("A customer may order a product for several times",
                3, shop.getNumberOfTimesProductWasOrdered(reSharper))
    }

    @Test fun testNumberOfTimesEachProductWasOrderedForRepeatedInOrderProduct() {
        Assert.assertEquals("An order may contain a particular product more than once",
                3, shop.getNumberOfTimesProductWasOrdered(phpStorm))
    }
}
