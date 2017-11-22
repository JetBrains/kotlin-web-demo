/**
 * This is where you'll add the definition of your state object. Look at the unit tests in [IOUStateTests] for
 * instructions on how to complete the [IOUState] class.
 *
 * Remove the "val data: String = "data" property before starting the [IOUState] tasks.
 */

package net.corda.training.state

import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import net.corda.core.identity.AbstractParty
import java.util.Currency





data class IOUState(<taskWindow>val data: String = "data"</taskWindow>): ContractState {
    override val participants: List<AbstractParty> get() = listOf()
}
