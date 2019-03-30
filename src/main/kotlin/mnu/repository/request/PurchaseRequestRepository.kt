package mnu.repository.request

import mnu.model.Client
import mnu.model.request.PurchaseRequest
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param

interface PurchaseRequestRepository : JpaRepository <PurchaseRequest, Long>{
 //   fun findAllByClient(client: Client) : List<PurchaseRequest>
/*
    @Query("select r.id, pr.requester_id, sc.id from purchase_requests pr" +
            " inner join requests r on (pr.request_id = r.id)" +
            " inner join shopping_carts sc on (pr.cart_id = sc.id)" +
            " inner join clients cl on (pr.requester_id = cl.id)" +
            " where ((cl.manager_id = :man) and (r.status = 'PENDING'))")
    fun getAllPendingRequestsForManagerByClient(@Param("man") managerId: Long) : List<Array<Any>>
*/}