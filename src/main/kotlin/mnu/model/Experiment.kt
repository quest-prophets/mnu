package mnu.model

import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentStatus
import mnu.model.enums.ExperimentType
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "experiments")
data class Experiment (@Column(nullable = false) var title: String = "",
                       var type: ExperimentType = ExperimentType.MINOR,
                       var description: String = "",
                       var date: LocalDateTime = LocalDateTime.now(),

                       @ManyToOne(fetch = FetchType.EAGER)
                       @JoinColumn(name = "examinator_id", referencedColumnName = "employee_user_id")
                       var examinator: ScientistEmployee? = null,

                       @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
                       @JoinColumn(name = "assistant_id", referencedColumnName = "employee_user_id")
                       var assistant: ScientistEmployee? = null)
{
    @Id
    @GeneratedValue
    var id: Long? = null

    var status: ExperimentStatus? = null
    var result: String? = null

}