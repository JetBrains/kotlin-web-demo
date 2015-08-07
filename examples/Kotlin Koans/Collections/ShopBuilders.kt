/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v_collections.shopBuilders

import v_collections.*

class ShopBuilder(val name: String) {
    val customers = arrayListOf<Customer>()
    fun build(): Shop = Shop(name, customers)
}

fun shop(name: String, init: ShopBuilder.() -> Unit): Shop {
    val shopBuilder = ShopBuilder(name)
    shopBuilder.init()
    return shopBuilder.build()
}

fun ShopBuilder.customer(name: String, city: City, init: CustomerBuilder.() -> Unit) {
    val customer = CustomerBuilder(name, city)
    customer.init()
    customers.add(customer.build())
}

class CustomerBuilder(val name: String, val city: City) {
    val orders = arrayListOf<Order>()
    fun build(): Customer = Customer(name, city, orders)
}

fun CustomerBuilder.order(isDelivered: Boolean, vararg products: Product) {
    orders.add(Order(products.toList(), isDelivered))
}

fun CustomerBuilder.order(vararg products: Product) = order(true, *products)