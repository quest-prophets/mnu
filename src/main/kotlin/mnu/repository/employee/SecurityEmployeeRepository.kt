package mnu.repository.employee

import mnu.model.employee.SecurityEmployee
import org.springframework.data.jpa.repository.*

interface SecurityEmployeeRepository : JpaRepository<SecurityEmployee, Long>{
    @Query(value = "select e.user_id, e.name, e.level, s.position, e.salary from employees e " +
            "inner join security_employees s on (e.user_id = s.employee_user_id) where (e.status = 'WORKING');", nativeQuery = true)
    fun getAllWorkingSecurity() : List<Array<Any>>

    @Query(value = "select count(*) from security_employees s inner join security_in_incidents sii on (s.employee_user_id = sii.security_id)" +
            " where (s.employee_user_id = ?1);", nativeQuery = true)
    fun allIncidentsParticipatedIn(id: Long) : Long
}