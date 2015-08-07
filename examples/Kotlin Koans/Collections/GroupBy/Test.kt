import org.junit.Assert
import org.junit.Test as test

class TestGroupBy {
    test fun testGroupCustomersByCity() {
        Assert.assertEquals(groupedByCities, shop.groupCustomersByCity())
    }
}
