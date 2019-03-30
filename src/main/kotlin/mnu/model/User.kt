package mnu.model

import mnu.model.enums.Role
import javax.persistence.*

@Entity
@Table(name = "users")
data class User (@Column(nullable = false, unique = true) var login: String = "",
                 var password: String = "") {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    var role: Role? = null
}