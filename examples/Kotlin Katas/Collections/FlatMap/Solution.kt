fun Customer.getOrderedProducts(): Set<Product> = orders.flatMap { it.products }.toSet()

fun Shop.getAllOrderedProducts(): Set<Product> = customers.flatMap { it.getOrderedProducts() }.toSet()