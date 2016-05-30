fun Shop.getCustomerWithMaximumNumberOfOrders(): Customer? = customers.maxBy { it.orders.size }

fun Customer.getMostExpensiveOrderedProduct(): Product? = orders.flatMap { it.products }.maxBy { it.price }
