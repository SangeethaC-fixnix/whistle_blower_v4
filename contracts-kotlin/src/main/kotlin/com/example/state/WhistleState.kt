package com.example.state

import com.example.contract.WhistleContract
import com.example.schema.OrderSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState


@BelongsToContract(WhistleContract::class)
data class WhistleState(val complaintId: String,
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
                          val person: Party,
                          val reviewer:Party,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState {
    override val participants: List<AbstractParty>
        get() = listOf(person,reviewer)
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when(schema)
        {
            is OrderSchemaV1 -> OrderSchemaV1.PersistantOrder(
                    this.complaintId,
                    this.companyName,
                    this.incidentType,
                    this.association,
                    this.awareOf,
                    this.personInvolved,
                    this.monetorValue,
                    this.date,
                    this.generalNature,
                    this.occurancePlace,
                    this.monetorValue,
                    this.person.toString(),
                    this.reviewer.toString(),
                    this.linearId.id
            )
            else-> throw IllegalArgumentException("There is no schemas found")
        }
    }


    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(OrderSchemaV1)

    }
}



