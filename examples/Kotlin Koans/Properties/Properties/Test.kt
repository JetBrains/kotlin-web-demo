import org.junit.Assert
import org.junit.Test

class TestProperties {
    @Test fun testPropertyWithCounter() {
        val q = PropertyExample()
        q.propertyWithCounter = 14
        q.propertyWithCounter = 21
        q.propertyWithCounter = 32
        Assert.assertTrue("The property 'changeCounter' should contain the number of assignments to 'propertyWithCounter'",
                3 == q.counter)
        // Here we have to use !! due to false smart cast impossible
        Assert.assertTrue("The property 'propertyWithCounter' should be set", 32 == q.propertyWithCounter!!)
    }

}