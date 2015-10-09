import org.junit.Assert
import org.junit.Test as test

class TestHtmlBuilders {
    @test fun productTableIsFilled() {
        val result = renderProductTable()
        Assert.assertTrue("Product table should contain the corresponding data", result.contains("cactus"))
    }

    @test fun productTableIsColored() {
        val result = renderProductTable()
        Assert.assertTrue("Product table should be colored", result.contains("bgcolor"))
    }
}
