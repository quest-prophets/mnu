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
                       @JoinColumn(name = "examinator_id", referencedColumnName = "id")
                       var examinator: ScientistEmployee? = null,

                       @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
                       @JoinTable(name = "assistants_in_experiments",
                            joinColumns = [JoinColumn(name = "experiment_id")],
                            inverseJoinColumns = [JoinColumn(name = "assistant_id")])
                       var assistants: List<ScientistEmployee>? = null)
{
    @Id
    @GeneratedValue
    var id: Long? = null

    var status: ExperimentStatus? = null
    var result: String? = null

}