/**
 * Here's an example of a very basic state. As you can see, there is nothing required beyond implementing one of the
 * Corda State interfaces. We start with the most basic type - the Contract State. This requires that you implement
 * the [participants] get function. For this, we're just going to return an empty list.
 */

package education

import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

data class IOUState(val data: String = "data"): ContractState {
    override val participants: List<AbstractParty> get() = listOf()
}

fun main(args: Array<String>) {
    val myFirstState = IOUState("This is where you would put data in relevant to this instance of a state.")
    println(myFirstState) // Would be a waste just to destroy it straight away...
}
