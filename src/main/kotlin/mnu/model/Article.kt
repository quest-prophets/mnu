package mnu.model

import java.time.LocalDateTime
import javax.persistence.*

data class Article (@Column(nullable = false) var title: String = "",
                    var text: String = "",
                    var creationDate: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scientist_id", referencedColumnName = "id")
    var scientist: ScientistEmployee? = null

}