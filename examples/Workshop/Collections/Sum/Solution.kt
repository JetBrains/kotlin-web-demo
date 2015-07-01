package v_collections

fun Customer.getTotalOrderPrice(): Double = orders.flatMap { it.products }.map { it.price }.sum()