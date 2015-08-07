package v_collections

val Customer.orderedProducts: Set<Product> get() = orders.flatMap { it.products }.toSet()

val Shop.allOrderedProducts: Set<Product> get() = customers.flatMap { it.orderedProducts }.toSet()