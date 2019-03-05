package mnu.model

import mnu.model.enums.*
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "clients")
data class Client (@Column(nullable = false) var name: String = "",
                   var password: String = "",
                   var email: String = "",
                   @Enumerated(EnumType.STRING) var gender: Gender = Gender.MALE,
                   @Enumerated(EnumType.STRING) var type: ClientType = ClientType.CLIENT,
                   var dateOfBirth: LocalDateTime? = null) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    var manager: ManagerEmployee? = null
}