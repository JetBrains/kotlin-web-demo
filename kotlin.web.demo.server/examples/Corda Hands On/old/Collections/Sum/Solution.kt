fun Customer.getTotalOrderPrice(): Double =
    orders.flatMap { it.products }.sumByDouble { it.price }