package mnu.model.request

import mnu.model.employee.Employee
import mnu.model.enums.RequestStatus
import javax.persistence.*

@Entity
@Table (name = "requests")
class Request {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    var status: RequestStatus? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolver_id", referencedColumnName = "user_id")
    var resolver: Employee? = null
}