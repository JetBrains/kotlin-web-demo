package iv_builders

import org.junit.Test as test
import junit.framework.Assert
import util.questions.Answer.*

class _26_Builders_How_It_Works {
    test fun testBuildersQuiz() {
        val correctAnswers = linkedMapOf(1 to c, 2 to b, 3 to b, 4 to c)
        Assert.assertEquals("Your answers are incorrect: ", correctAnswers, answers)
    }
}