package mnu.model.employee

import mnu.model.Article
import mnu.model.Experiment
import javax.persistence.*

@Entity
@Table(name = "scientists")
class ScientistEmployee {
    @Id
    var employeeId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private val employee: Employee? = null

    var position: String? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "scientist")
    var articles: MutableList<Article>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "examinator")
    var conductedExperiments: MutableList<Experiment>? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "assistants")
    var assistedExperiments: MutableList<Experiment>? = null
}