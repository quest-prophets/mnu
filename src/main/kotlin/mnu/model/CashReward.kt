package mnu.model

import mnu.model.employee.Employee
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "cash_rewards")
data class CashReward (@ManyToOne(fetch = FetchType.EAGER)
                       @JoinColumn(name = "employee_id", referencedColumnName = "user_id")
                       var employee: Employee? = null,

                       @Min(1) var reward: Long = 1,
                       var issueDate: LocalDateTime = LocalDateTime.now()){
    @Id
    @GeneratedValue
    var id: Long? = null
}