package com.template.states

import com.template.contracts.ComplaintContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********


import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********

data class ComplaintState(val complaintId: String,
                          val companyName:String,
                          val incidentType:String,
                          val association: String,
                          val awareOf:String,
                          val personInvolved: String,
                          val monetorValue: String,
                          val date: String,
                          val auditAware:String,
                          val generalNature:String,
                          val occurancePlace:String,
                          val person:Party,
                          val reviewer:Party,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants: List<AbstractParty>
        get() = listOf(person,reviewer)


}

