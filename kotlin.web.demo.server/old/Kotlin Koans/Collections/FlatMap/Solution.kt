val Customer.orderedProducts: Set<Product> get() {
    return orders.flatMap { it.products }.toSet()
}

val Shop.allOrderedProducts: Set<Product> get() {
    return customers.flatMap { it.orderedProducts }.toSet()
}