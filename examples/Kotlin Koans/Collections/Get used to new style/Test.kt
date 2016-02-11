import org.junit.Test
import org.junit.Assert
import koans.util.inEquals

class TestExtensionsOnCollections {
    @Test fun testCollectionOfOneElement() {
        doTest(listOf("a"), listOf("a"))
    }

    @Test fun testSimpleCollection() {
        doTest(listOf("a", "c"), listOf("a", "bb", "c"))
    }

    @Test fun testCollectionWithEmptyStrings() {
        doTest(listOf("", "", "", ""), listOf("", "", "", "", "a", "bb", "ccc", "dddd"))
    }

    @Test fun testCollectionWithTwoGroupsOfMaximalSize() {
        doTest(listOf("a", "c"), listOf("a", "bb", "c", "dd"))
    }

    private fun doTest(expected: Collection<String>?, argument: Collection<String>) {
        Assert.assertEquals("The function 'doSomethingStrangeWithCollection' should do at least something with a collection".inEquals(),
                expected, doSomethingStrangeWithCollection(argument))
    }
}