package mnu.repository.employee

import mnu.model.employee.AdministratorEmployee
import org.springframework.data.jpa.repository.*

interface AdministratorEmployeeRepository : JpaRepository<AdministratorEmployee, Long> {

}