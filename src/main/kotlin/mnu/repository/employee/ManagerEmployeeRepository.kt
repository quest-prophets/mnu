package mnu.repository.employee

import mnu.model.employee.ManagerEmployee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ManagerEmployeeRepository : JpaRepository<ManagerEmployee, Long> {
    @Query(value = "select e.id, e.name, e.level, m.position, e.salary from employee e " +
            "inner join managers m on (e.id = m.employee_id) where (e.status = 'WORKING')")
    fun getAllWorkingManagers() : List<Array<Any>>

    @Query(value = "select count(*) from managers m inner join requests r on (m.employee_id = r.resolver_id)" +
            " inner join new_weapon_requests nwr on (r.id = nwr.request_id)" +
            " where (r.status = 'RESOLVED');", nativeQuery = true)
    fun allResolvedNewWeaponRequests() : Long

    @Query(value = "select count(*) from managers m inner join requests r on (m.employee_id = r.resolver_id)" +
            " inner join purchase_requests pr on (r.id = pr.request_id)" +
            " where (r.status = 'RESOLVED');", nativeQuery = true)
    fun allResolvedPurchaseRequests() : Long

    @Query(value = "select count(*) from managers m inner join requests r on (m.employee_id = r.resolver_id)" +
            " inner join change_equipment_requests cer on (r.id = cer.request_id)" +
            " where (r.status = 'RESOLVED');", nativeQuery = true)
    fun allResolvedChangeEquipmentRequests() : Long

    @Query(value = "select count(*) from managers m inner join requests r on (m.employee_id = r.resolver_id)" +
            " inner join vacancy_application_requests var on (r.id = var.request_id)" +
            " where (r.status = 'RESOLVED');", nativeQuery = true)
    fun allResolvedVacancyApplicationRequests() : Long

}