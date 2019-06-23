package com.example.contract

import com.example.state.WhistleState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [WhistleState], which in turn encapsulates an [WhistleState].
 *
 * For a new [WhistleState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [WhistleState].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class WhistleContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.StatesAndContracts.ComplaintContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic
        val Command=tx.commands.requireSingleCommand<Commands.ComplaintReg>()

        requireThat {
            "No input state should be allowed" using(tx.inputs.isEmpty())

            val complaint=tx.outputsOfType<WhistleState>().single()


            "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)
            "Monetory value should not be zero" using(complaint.monetorValue!="null")
            "All the participants must be signers" using(Command.signers.containsAll(complaint.participants.map{it.owningKey}))

        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class ComplaintReg : Commands
    }
}