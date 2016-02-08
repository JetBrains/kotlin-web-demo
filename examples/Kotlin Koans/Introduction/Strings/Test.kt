import org.junit.Test
import org.junit.Assert
import java.util.regex.Pattern

class TestStringTemplates() {
    @Test fun match() {
        Assert.assertTrue("11 MAR 1952".matches(getPattern().toRegex()))
    }

    @Test fun match1() {
        Assert.assertTrue("24 AUG 1957".matches(getPattern().toRegex()))
    }

    @Test fun doNotMatch() {
        Assert.assertFalse("24 RRR 1957".matches(getPattern().toRegex()))
    }}