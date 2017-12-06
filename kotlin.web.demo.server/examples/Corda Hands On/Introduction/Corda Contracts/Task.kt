package net.corda.training.contract

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.utils.sumCash
import java.util.Currency

data class IOUState(val amount: Amount<Currency>,
                    val lender: Party,
                    val borrower: Party,
                    val paid: Amount<Currency> = Amount(0, amount.token),
                    override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  lender or the borrower.
     */
    override val participants get() = listOf(lender, borrower)

    /**
     * A toString() helper method for displaying IOUs in the console.
     */
    override fun toString() = "IOU($linearId): ${borrower.name} owes ${lender.name} $amount and has paid $paid so far."

    /**
     * Helper methods for when building transactions for settling and transferring IOUs.
     * - [pay] adds an amount to the paid property. It does no validation.
     * - [withNewLender] creates a copy of the current state with a newly specified lender. For use when transferring.
     */
    fun pay(amountToPay: Amount<Currency>) = copy(paid = paid.plus(amountToPay))
    fun withNewLender(newLender: Party) = copy(lender = newLender)
}


/**
 * The IOUContract can handle three transaction types involving [IOUState]s.
 * - Issuance: Issuing a new [IOUState] on the ledger, which is a bilateral agreement between two parties.
 * - Transfer: Re-assigning the lender/beneficiary.
 * - Settle: Fully or partially settling the [IOUState] using the Corda [Cash] contract.
 *
 * LegalProseReference: this is just a dummy string for the time being.
 */
@LegalProseReference(uri = "<prose_contract_uri>")
class IOUContract : Contract {
    companion object {
        @JvmStatic
        val IOU_CONTRACT_ID = "net.corda.training.contract.IOUContract"
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class Transfer : TypeOnlyCommandData(), Commands
        class Settle : TypeOnlyCommandData(), Commands
    }

    /**
     * The contract code for the [IOUContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<IOUContract.Commands>()
        <taskWindow>
        when (command.value) {
            is Commands.Issue -> requireThat {
              // Your code here
            }
            is Commands.Transfer -> requireThat {
              // and here
            }
            is Commands.Settle -> {
              // oh.. here too.
            }
            </taskWindow>
        }
    }
}
