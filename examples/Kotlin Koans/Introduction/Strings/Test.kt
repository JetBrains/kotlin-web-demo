import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert
import java.util.regex.Pattern

class TestStringTemplates() {
    @test fun match() {
        Assert.assertTrue(Pattern.compile(getPattern()).matcher("11 MAR 1952").find())
    }

    @test fun match1() {
        Assert.assertTrue(Pattern.compile(getPattern()).matcher("24 AUG 1957").find())
    }

    @test fun doNotMatch() {
        Assert.assertFalse(Pattern.compile(getPattern()).matcher("24 RRR 1957").find())
    }
}