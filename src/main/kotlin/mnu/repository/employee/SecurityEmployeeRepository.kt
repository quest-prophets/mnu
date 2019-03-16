package mnu.repository.employee

import mnu.model.employee.SecurityEmployee
import org.springframework.data.jpa.repository.*

interface SecurityEmployeeRepository : JpaRepository<SecurityEmployee, Long>{
    @Query(value = "select e.id, e.name, e.level, s.position, e.salary from employee e " +
            "inner join security_employees s on (e.id = s.employee_id) where (e.status = 'WORKING')")
    fun getAllWorkingSecurity() : List<Array<Any>>

    @Query(value = "select count(*) from security_employees s inner join security_in_incidents sii on (s.id = sii.security_id);", nativeQuery = true)
    fun allIncidentsParticipatedIn(id: Long) : Long
}