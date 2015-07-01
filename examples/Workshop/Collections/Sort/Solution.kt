package v_collections

fun Shop.getCustomersSortedByNumberOfOrders(): List<Customer> = customers.sortBy { it.orders.size }