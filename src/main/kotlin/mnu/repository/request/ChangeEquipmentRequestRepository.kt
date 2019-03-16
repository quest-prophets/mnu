package mnu.repository.request

import mnu.model.Transport
import mnu.model.Weapon
import mnu.model.employee.SecurityEmployee
import mnu.model.request.ChangeEquipmentRequest
import org.springframework.data.jpa.repository.*

interface ChangeEquipmentRequestRepository : JpaRepository<ChangeEquipmentRequest, Long>{
    fun findAllByEmployee(employee: SecurityEmployee) : List<ChangeEquipmentRequest>

    fun findAllByWeapon(weapon: Weapon) : List<ChangeEquipmentRequest>

    fun findAllByTransport(transport: Transport) : List<ChangeEquipmentRequest>

    @Query("select r.id, cer.requester_id, cer.new_weapon_id, cer.new_transport_id from change_equipment_requests cer" +
            " inner join requests r on (cer.request_id = r.id)" +
            " where (r.status = 'PENDING')")
    fun getAllPendingRequests() : List<Array<Any>>

}