import org.junit.Test as test
import org.junit.Assert

class TestExtensionsOnCollections {
    @test fun testCollectionOfOneElement() {
        doTest(listOf("a"), listOf("a"))
    }

    @test fun testSimpleCollection() {
        doTest(listOf("a", "c"), listOf("a", "bb", "c"))
    }

    @test fun testCollectionWithEmptyStrings() {
        doTest(listOf("", "", "", ""), listOf("", "", "", "", "a", "bb", "ccc", "dddd"))
    }

    @test fun testCollectionWithTwoGroupsOfMaximalSize() {
        doTest(listOf("a", "c"), listOf("a", "bb", "c", "dd"))
    }

    private fun doTest(expected: Collection<String>?, argument: Collection<String>) {
        Assert.assertEquals("The function 'doSomethingStrangeWithCollection' should do at least something with a collection:",
                expected, doSomethingStrangeWithCollection(argument))
    }
}