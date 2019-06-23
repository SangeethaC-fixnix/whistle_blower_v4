package com.example.server

import com.example.flow.ComplaintCreateFlow.Initiator
import com.example.state.WhistleState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/fixnix/") // The paths for GET and POST requests are relative to this base path.
class MainController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GetMapping(value = [ "whistles" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getIOUs() : ResponseEntity<List<StateAndRef<WhistleState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<WhistleState>().states)
    }



    @PostMapping(value = [ "create-iou" ], produces = [ TEXT_PLAIN_VALUE ], headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun createIOU(request: HttpServletRequest): ResponseEntity<String> {
        val complaint_Id = request.getParameter("complaint_Id")
        val company_Name = request.getParameter("company_Name")
        val general_Nature = request.getParameter("general_Nature")
        val occurance_Place = request.getParameter("occurance_Place")
        val reviewer = request.getParameter("reviewer")

        val partyX500Name = CordaX500Name.parse(reviewer)
        val otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name) ?: return ResponseEntity.badRequest().body("Party named $reviewer cannot be found.\n")

        if(complaint_Id == null){
            return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n")
        }

        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, complaint_Id, company_Name, general_Nature, occurance_Place, otherParty).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = [ "my-whistles" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyIOUs(): ResponseEntity<List<StateAndRef<WhistleState>>>  {
        val myious = proxy.vaultQueryBy<WhistleState>().states.filter { it.state.data.complaintId.equals(proxy.nodeInfo().legalIdentities.first())  }
        return ResponseEntity.ok(myious)
    }

}
