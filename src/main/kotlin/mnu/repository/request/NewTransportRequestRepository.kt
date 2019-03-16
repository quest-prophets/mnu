package mnu.repository.request

import mnu.model.Client
import mnu.model.request.NewTransportRequest
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param

interface NewTransportRequestRepository : JpaRepository<NewTransportRequest, Long>{
    fun findAllByClient(client: Client) : List<NewTransportRequest>

    @Query("select r.id, ntr.requester_id, ntr.name, ntr.type, ntr.description, ntr.quantity, ntr.required_access_lvl from new_transport_requests ntr" +
            " inner join requests r on (ntr.request_id = r.id)" +
            " inner join clients cl on (ntr.requester_id = cl.id)" +
            " where ((cl.manager_id = :man) and (r.status = 'PENDING'))")
    fun getAllPendingRequestsForManagerByClient(@Param("man") managerId: Long) : List<Array<Any>>

}