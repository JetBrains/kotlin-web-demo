fun Shop.getProductsOrderedByAllCustomers(): Set<Product> {
    val allProducts = customers.flatMap { it.orders.flatMap { it.products }}.toSet()
    return customers.fold(allProducts, {
        orderedByAll, customer ->
        orderedByAll.intersect(customer.orders.flatMap { it.products }.toSet())
    })
}