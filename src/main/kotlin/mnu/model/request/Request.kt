package mnu.model.request

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
}