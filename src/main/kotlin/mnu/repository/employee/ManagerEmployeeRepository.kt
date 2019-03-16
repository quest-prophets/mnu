package mnu.repository.employee

import mnu.model.employee.ManagerEmployee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ManagerEmployeeRepository : JpaRepository<ManagerEmployee, Long> {
    @Query(value = "select e.id, e.name, e.level, m.position, e.salary from employee e " +
            "inner join managers m on (e.id = m.employee_id) where (e.status = 'WORKING')")
    fun getAllWorkingManagers() : List<Array<Any>>

    @Query(value = "select count(*) from managers m inner join clients c on (m.id = c.manager_id)" +
            " inner join prawns p on (m.id = p.manager_id)" +
            " inner join new_transport_requests ntr on (c.id = ntr.requester_id)" +
            " inner join new_weapon_requests nwr on (c.id = nwr.requester_id)" +
            " inner join new_vacancy_requests nvr on (c.id = nvr.requester_id)" +
            " inner join purchase_requests pr on (c.id = pr.requester_id)" +
            " inner join change_equipment_requests cer ntr on (m.id = cer.manager_id)" +
            " inner join vacancy_application_requests var on (p.id = var.prawn_id);", nativeQuery = true)
    fun allResolvedRequests(): Long
}