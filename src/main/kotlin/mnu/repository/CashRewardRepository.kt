package mnu.repository

import mnu.model.CashReward
import mnu.model.employee.Employee
import org.springframework.data.jpa.repository.*

interface CashRewardRepository : JpaRepository<CashReward, Long> {
    fun findAllByEmployee (employee: Employee) : List<CashReward>

    fun findAllByEmployeeOrderByIssueDateDesc (employee: Employee) : List<CashReward>?
}