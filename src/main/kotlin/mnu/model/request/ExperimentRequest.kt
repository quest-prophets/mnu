package mnu.model.request

import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table (name = "experiment_requests")
data class ExperimentRequest (@Column(nullable = false) var title: String = "",
                              var type: ExperimentType = ExperimentType.MINOR,
                              var description: String = "",
                              var date: LocalDateTime = LocalDateTime.now(),

                              @ManyToOne(fetch = FetchType.EAGER)
                              @JoinColumn(name = "examinator_id", referencedColumnName = "employee_user_id")
                              var examinator: ScientistEmployee? = null,

                              @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
                              @JoinTable(name = "assistants_in_experiment_requests",
                                  joinColumns = [JoinColumn(name = "request_id")],
                                  inverseJoinColumns = [JoinColumn(name = "assistant_id")])
                              var assistants: List<ScientistEmployee>? = null) {
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private val request: Request? = null
}