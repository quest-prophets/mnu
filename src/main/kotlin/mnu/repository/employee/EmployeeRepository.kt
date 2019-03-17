package mnu.repository.employee

import mnu.model.employee.Employee
import mnu.model.enums.PersonStatus
import org.springframework.data.jpa.repository.*

interface EmployeeRepository : JpaRepository <Employee, Long> {
    fun findByLogin (login: String) : Employee

    fun findAllBySalaryGreaterThanEqual (salary: Long) : List<Employee>

    fun findAllByLevelLessThanEqualAndLevelGreaterThanEqual (levelLess: Long, levelGreater: Long) : List<Employee>

    fun findAllByStatus (status: PersonStatus) : List<Employee>

}