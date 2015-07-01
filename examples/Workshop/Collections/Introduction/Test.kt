package v_collections

import junit.framework.Assert
import org.junit.Test as test
import v_collections.data.*

class A_Introduction {
    test fun testSetOfCustomers(){
        val setOfCustomers = shop.getSetOfCustomers()
        Assert.assertTrue(setOfCustomers is Set<Customer>)
        customers.values().forEach { Assert.assertTrue(it in setOfCustomers) }
    }
}
