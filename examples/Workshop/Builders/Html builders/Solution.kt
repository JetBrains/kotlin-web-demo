package iv_builders

import iv_builders.data.getProducts
import iv_builders.htmlLibrary.*

fun renderProductTable(): String {
    return html {
        table {
            tr (color = getTitleColor()) {
                td {
                    text("Product")
                }
                td {
                    text("Price")
                }
                td {
                    text("Popularity")
                }
            }
            val products = getProducts()
            for ((index, product) in products.withIndex()) {
                tr {
                    td (color = getCellColor(index, 0)) {
                        text(product.description)
                    }
                    td (color = getCellColor(index, 1)) {
                        text(product.price)
                    }
                    td (color = getCellColor(index, 2)) {
                        text(product.popularity)
                    }
                }
            }
        }
    }.toString()
}

fun getTitleColor() = "#b9c9fe"
fun getCellColor(index: Int, row: Int) = if ((index + row) %2 == 0) "#dce4ff" else "#eff2ff"