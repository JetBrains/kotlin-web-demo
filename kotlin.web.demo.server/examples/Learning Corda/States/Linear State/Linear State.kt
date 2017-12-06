

/**
 * So we're now expanding on our concept of a [ContractState] by introducing a [LinearState] otherwise known as a state that will
 * evolve over time. To track this in our queries, we'll need a reference to this state, which we are calling the [linearId]
 * If you want to refer to the latest version of this state from external systems, you'll need to keep track of this.
 */

package education

import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.finance.POUNDS
import net.corda.testing.ALICE
import net.corda.testing.BOB
import org.junit.Test
import java.util.Currency

data class IOUState(val lender: Party,
                    val borrower: Party,
                    val amount: Amount<Currency>,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<AbstractParty> get() = listOf()


}

fun main(args: Array<String>) {
    val myFirstLinearState = IOUState(ALICE, BOB, 100.POUNDS)
    println(myFirstLinearState) // Would be a waste just to destroy it straight away...
}

