package mnu.model.employee

import mnu.model.Article
import javax.persistence.*

@Entity
@Table(name = "scientists")
class ScientistEmployee {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    var employee: Employee? = null

    var position: String? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "scientist")
    var articles: MutableList<Article>? = null
}