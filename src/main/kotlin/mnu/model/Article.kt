package mnu.model

import mnu.model.employee.ScientistEmployee
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "articles")
data class Article (@Column(nullable = false) var title: String = "",
                    var text: String = "",
                    var creationDate: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scientist_id", referencedColumnName = "employee_user_id")
    var scientist: ScientistEmployee? = null

}