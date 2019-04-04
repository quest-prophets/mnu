package mnu.model.employee

import mnu.model.Article
import mnu.model.Experiment
import javax.persistence.*

@Entity
@Table(name = "scientists")
data class ScientistEmployee (var position: String? = null){
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var employee: Employee? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "scientist")
    var articles: MutableList<Article>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "examinator")
    var conductedExperiments: MutableList<Experiment>? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "assistants")
    var assistedExperiments: MutableList<Experiment>? = null
}