package mnu.repository.request

import mnu.model.Client
import mnu.model.User
import mnu.model.request.NewWeaponRequest
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param

interface NewWeaponRequestRepository : JpaRepository<NewWeaponRequest, Long>{
    fun findAllByUser(user: User) : List<NewWeaponRequest>?

//    @Query("select r.id, nwr.requester_id, nwr.name, nwr.type, nwr.description, nwr.quantity, nwr.required_access_lvl from new_weapon_requests nwr" +
//            " inner join requests r on (nwr.request_id = r.id)" +
//            " inner join clients cl on (nwr.requester_id = cl.id)" +
//            " where ((cl.manager_id = ?1) and (r.status = 'PENDING'));", nativeQuery = true)
//    fun getAllPendingRequestsForManagerByClient(managerId: Long) : List<Array<Any>>

}