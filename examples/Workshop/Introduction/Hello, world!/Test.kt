package i_introduction._0_Hello_World.Hello

import org.junit.Assert
import org.junit.Test as test

class _00_Start {
    test fun testOk() {
        Assert.assertEquals("Hello, world!", sayHello())
    }
}