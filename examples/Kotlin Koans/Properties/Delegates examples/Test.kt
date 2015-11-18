import org.junit.Assert
import org.junit.Test
import java.util.HashMap

class TestDelegatesExamples {
    @Test fun testLazy() {
        var initialized = false
        val lazyProperty = LazyProperty({ initialized = true; 42 })
        Assert.assertFalse("Property shouldn't be initialized before access", initialized)
        val result: Int = lazyProperty.lazyValue
        Assert.assertTrue("Property should be initialized after access", initialized)
        Assert.assertEquals(42, result)
    }

    @Test fun initializedOnce() {
        var initialized = 0
        val lazyProperty = LazyProperty( { initialized++; 42 })
        lazyProperty.lazyValue
        lazyProperty.lazyValue
        Assert.assertEquals("Lazy property should be initialized once", 1, initialized)
    }
}