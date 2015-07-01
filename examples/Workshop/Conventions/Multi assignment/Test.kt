package ii_conventions

import junit.framework.Assert
import org.junit.Test as test

class _16_Multi_Assignment {
    test fun testTask16() {
        Assert.assertEquals(listOf(1, 2, 3, 4), addIndex(listOf(1, 1, 1, 1)))
    }
}