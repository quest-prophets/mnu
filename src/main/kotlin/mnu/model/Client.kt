package mnu.model

import mnu.model.employee.ManagerEmployee
import mnu.model.enums.*
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "clients")
data class Client (@Column(nullable = false) var name: String = "",
                   @Column(nullable = false, unique = true) var email: String = "",
                   @Enumerated(EnumType.STRING) var type: ClientType = ClientType.CLIENT) {
    @Id
    var userId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    val user: User? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id", referencedColumnName = "employee_id")
    var manager: ManagerEmployee? = null
}