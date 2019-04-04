package mnu.model

import mnu.model.employee.ManagerEmployee
import mnu.model.enums.*
import javax.persistence.*

@Entity
@Table(name = "clients")
data class Client (@Column(nullable = false) var name: String = "",
                   @Column(nullable = false, unique = true) var email: String = "",
                   @Enumerated(EnumType.STRING) var type: ClientType = ClientType.CUSTOMER) {
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var user: User? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id", referencedColumnName = "employee_user_id")
    var manager: ManagerEmployee? = null
}