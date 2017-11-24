fun Shop.getCitiesCustomersAreFrom(): Set<City> =
    customers.map { it.city }.toSet()

fun Shop.getCustomersFrom(city: City): List<Customer> =
    customers.filter { it.city == city }