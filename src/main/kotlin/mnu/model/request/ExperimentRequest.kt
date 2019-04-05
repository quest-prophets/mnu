package mnu.model.request

import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table (name = "experiment_requests")
data class ExperimentRequest (@Column(nullable = false) var title: String = "",
                              var type: ExperimentType = ExperimentType.MINOR,
                              var description: String? = "",
                              var date: LocalDateTime = LocalDateTime.now(),

                              @ManyToOne(fetch = FetchType.LAZY)
                              @JoinColumn(name = "examinator_id", referencedColumnName = "employee_user_id")
                              var examinator: ScientistEmployee? = null,

                              @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
                              @JoinColumn(name = "assistant_id", referencedColumnName = "employee_user_id")
                              var assistant: ScientistEmployee? = null) {
    @Id
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var request: Request? = null
}