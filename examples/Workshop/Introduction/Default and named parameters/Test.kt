package i_introduction._2_Default_And_Named_Params

import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert

class _02_Default_And_Named_Params() {

    test fun testJoinToString() {
        Assert.assertEquals("[1, 2, 3, 42, 555]", toJSON(listOf(1, 2, 3, 42, 555)))
    }

}