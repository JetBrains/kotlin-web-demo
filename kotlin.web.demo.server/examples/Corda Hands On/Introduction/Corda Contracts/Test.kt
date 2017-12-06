package net.corda.training.contract

import org.junit.*
import net.corda.core.contracts.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.finance.*
import net.corda.finance.contracts.asset.DUMMY_CASH_ISSUER
import net.corda.testing.*
import net.corda.testing.contracts.DummyContract
import net.corda.testing.contracts.DummyState

/**
 * Practical exercise instructions for Contracts Part 1.
 * The objective here is to write some contract code that verifies a transaction to issue an [IOUState].
 * As with the [IOUStateTests] uncomment each unit test and run them one at a time. Use the body of the tests and the
 * task description to determine how to get the tests to pass.
 */
class TestStart {
    // A pre-defined dummy command.
    class DummyCommand : TypeOnlyCommandData()

    @Before
    fun setup() {
        setCordappPackages("net.corda.training")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }


    /**
     * Task 1.
     * Recall that Commands are required to hint to the intention of the transaction as well as take a list of
     * public keys as parameters which correspond to the required signers for the transaction.
     * Commands also become more important later on when multiple actions are possible with an IOUState, e.g. Transfer
     * and Settle.
     * TODO: Add an "Issue" command to the IOUState and check for the existence of the command in the verify function.
     * Hint:
     * - For the create command we only care about the existence of it in a transaction, therefore it should subclass
     *   the [TypeOnlyCommandData] class.
     * - The command should be defined inside [IOUContract].
     * - You can use the [requireSingleCommand] function on [tx.commands] to check for the existence and type of the specified command
     *   in the transaction.
     * - We usually encapsulate our commands around an interface inside the contract class called [Commands] which
     *   implements the [CommandData] interface. The [Create] command itself should be defined inside the [Commands]
     *   interface as well as implement it, for example:
     *
     *     interface Commands : CommandData {
     *         class X : TypeOnlyCommandData(), Commands
     *     }
     *
     * - We can check for the existence of any command that implements [IOUContract.Commands] by using the
     *   [requireSingleCommand] function which takes a type parameter.
     */
    @Test
    fun mustIncludeIssueCommand() {
        val iou = IOUState(1.POUNDS, ALICE, BOB)
        ledger {
            transaction {
                output(IOUContract.IOU_CONTRACT_ID){ iou }
                command(ALICE_PUBKEY, BOB_PUBKEY) { DummyCommand() } // Wrong type.
                this.fails()
            }
            transaction {
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() } // Correct type.
                this.verifies()
            }
        }
    }

    /**
     * Task 2.
     * As previously observed, issue transactions should not have any input state references. Therefore we must check to
     * ensure that no input states are included in a transaction to issue an IOU.
     * TODO: Write a contract constraint that ensures a transaction to issue an IOU does not include any input states.
     * Hint: use a [requireThat] block with a constraint to inside the [IOUContract.verify] function to encapsulate your
     * constraints:
     *
     *     requireThat {
     *         "Message when constraint fails" using (boolean constraint expression)
     *     }
     *
     * Note that the unit tests often expect contract verification failure with a specific message which should be
     * defined with your contract constraints. If not then the unit test will fail!
     *
     * You can access the list of inputs via the [TransactionForContract] object which is passed into
     * [IOUContract.verify].
     */
    @Test
    fun issueTransactionMustHaveNoInputs() {
        val iou = IOUState(1.POUNDS, ALICE, BOB)
        ledger {
            transaction {
                input(IOUContract.IOU_CONTRACT_ID) { DummyState() }
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "No inputs should be consumed when issuing an IOU."
            }
            transaction {
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                this.verifies() // As there are no input states.
            }
        }
    }

    /**
     * Task 3.
     * Now we need to ensure that only one [IOUState] is issued per transaction.
     * TODO: Write a contract constraint that ensures only one output state is created in a transaction.
     * Hint: Write an additional constraint within the existing [requireThat] block which you created in the previous
     * task.
     */
    @Test
    fun issueTransactionMustHaveOneOutput() {
        val iou = IOUState(1.POUNDS, ALICE, BOB)
        ledger {
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou } // Two outputs fails.
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "Only one output state should be created when issuing an IOU."
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou } // One output passes.
                this.verifies()
            }
        }
    }

    /**
     * Task 4.
     * Now we need to consider the properties of the [IOUState]. We need to ensure that an IOU should always have a
     * positive value.
     * TODO: Write a contract constraint that ensures newly issued IOUs always have a positive value.
     * Hint: You will nee da number of hints to complete this task!
     * - Use the Kotlin keyword 'val' to create a new constant which will hold a reference to the output IOU state.
     * - You can use the Kotlin function [single] to either grab the single element from the list or throw an exception
     *   if there are 0 or more than one elements in the list. Note that we have already checked the outputs list has
     *   only one element in the previous task.
     * - We need to obtain a reference to the proposed IOU for issuance from the [TransactionForContract.inputs] list.
     *   This list is typed as a list of [ContractState]s, therefore we need to cast the [ContractState] which we return
     *   from [single] to an [IOUState]. You can use the Kotlin keyword 'as' to cast a class. E.g.
     *
     *       val state = tx.inputs.single() as XState
     *
     * - When checking the [IOUState.amount] property is greater than zero, you need to check the
     *   [IOUState.amount.quantity] field.
     */
    @Test
    fun cannotCreateZeroValueIOUs() {
        ledger {
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { IOUState(0.POUNDS, ALICE, BOB) } // Zero amount fails.
                this `fails with` "A newly issued IOU must have a positive amount."
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { IOUState(100.SWISS_FRANCS, ALICE, BOB) }
                this.verifies()
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { IOUState(1.POUNDS, ALICE, BOB) }
                this.verifies()
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { IOUState(10.DOLLARS, ALICE, BOB) }
                this.verifies()
            }
        }
    }

    /**
     * Task 5.
     * The list of public keys which the commands hold should contain all of the participants defined in the [IOUState].
     * This is because the IOU is a bilateral agreement where both parties involved are required to sign to issue an
     * IOU or change the properties of an existing IOU.
     * TODO: Add a contract constraint to check that all the required signers are [IOUState] participants.
     * Hint:
     * - In Kotlin you can perform a set equality check of two sets with the == operator.
     * - We need to check that the signers for the transaction are a subset of the participants list.
     * - We don't want any additional public keys not listed in the IOUs participants list.
     * - You will need a reference to the Issue command to get access to the list of signers.
     * - [requireSingleCommand] returns the single required command - you can assign the return value to a constant.
     */
    @Test
    fun lenderAndBorrowerMustSignIssueTransaction() {
        val iou = IOUState(1.POUNDS, ALICE, BOB)
        ledger {
            transaction {
                command(DUMMY_KEY_1.public) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
            }
            transaction {
                command(ALICE_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
            }
            transaction {
                command(BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
            }
            transaction {
                command(BOB_PUBKEY, BOB_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
            }
            transaction {
                command(BOB_PUBKEY, BOB_PUBKEY, MINI_CORP_PUBKEY, ALICE_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
            }
            transaction {
                command(BOB_PUBKEY, BOB_PUBKEY, BOB_PUBKEY, ALICE_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this.verifies()
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this.verifies()
            }
        }
    }

    /**
     * Task 6.
     * For obvious reasons, the identity of the lender and borrower must be different.
     * TODO: Add a contract constraint to check the lender is not the borrower.
     * Hint:
     * - You can use the [IOUState.lender] and [IOUState.borrower] properties.
     * - This check must be made before the checking who has signed.
     */
    @Test
    fun lenderAndBorrowerCannotBeTheSame() {
        val iou = IOUState(1.POUNDS, ALICE, BOB)
        val borrowerIsLenderIou = IOUState(10.POUNDS, ALICE, ALICE)
        ledger {
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { borrowerIsLenderIou }
                this `fails with` "The lender and borrower cannot have the same identity."
            }
            transaction {
                command(ALICE_PUBKEY, BOB_PUBKEY) { IOUContract.Commands.Issue() }
                output(IOUContract.IOU_CONTRACT_ID) { iou }
                this.verifies()
            }
        }
    }
}
