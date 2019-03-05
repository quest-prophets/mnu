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
                       var description: String = "")
{
    @Id
    @GeneratedValue
    var id: Long? = null

    var status: ExperimentStatus? = null
    var date: LocalDateTime? = null
    var result: String? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "house_id", referencedColumnName = "id")
    var examinator: ScientistEmployee? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(name = "assistants_in_experiments",
        joinColumns = [JoinColumn(name = "experiment_id")],
        inverseJoinColumns = [JoinColumn(name = "assistant_id")]
    )
    var assistants: List<ScientistEmployee>? = null
}