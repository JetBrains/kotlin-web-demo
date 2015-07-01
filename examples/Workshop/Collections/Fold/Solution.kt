package v_collections

fun Shop.getProductsOrderedByAllCustomers(): Set<Product> {
    return customers.fold(customers.flatMap { it.orders.flatMap { it.products } }.toSet(), {
        orderedByAll, customer ->
        orderedByAll.intersect(customer.orders.flatMap { it.products }.toSet())
    })
}