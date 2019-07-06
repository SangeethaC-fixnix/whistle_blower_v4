package com.template.contracts

import com.template.states.ComplaintState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat


// ************
// * Contract *
// ************
class ComplaintContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "contract.ComplaintContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic
        val Command=tx.commands.requireSingleCommand<Commands.ComplaintReg>()

        requireThat {
            "No input state should be allowed" using(tx.inputs.isEmpty())

            val complaint=tx.outputsOfType<ComplaintState>().single()


            "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)
            "Monetory value should not be zero" using(complaint.monetorValue!="null")
            "the suspected person should be in the company" using(complaint.person.equals(complaint.companyName))
            "All the participants must be signers" using(Command.signers.containsAll(complaint.participants.map{it.owningKey}))
            "Audit should not be aware of" using(complaint.auditAware.equals("yes"))
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class ComplaintReg : Commands
    }

}