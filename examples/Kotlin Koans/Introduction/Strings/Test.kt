import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert
import java.util.regex.Pattern

class TestStringTemplates() {
    @test fun match() {
        Assert.assertTrue("11 MAR 1952".matches(getPattern().toRegex()))
    }

    @test fun match1() {
        Assert.assertTrue("24 AUG 1957".matches(getPattern().toRegex()))
    }

    @test fun doNotMatch() {
        Assert.assertFalse("24 RRR 1957".matches(getPattern().toRegex()))
    }}