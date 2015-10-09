fun Shop.checkAllCustomersAreFrom(city: City): Boolean = customers.all { it.city == city }

fun Shop.hasCustomerFrom(city: City): Boolean = customers.any { it.city == city }

fun Shop.countCustomersFrom(city: City): Int = customers.count { it.city == city }

fun Shop.findAnyCustomerFrom(city: City): Customer? = customers.firstOrNull { it.city == city }