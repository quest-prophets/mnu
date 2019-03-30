package mnu.model.request

import mnu.model.Prawn
import mnu.model.Vacancy
import javax.persistence.*

@Entity
@Table (name = "vacancy_application_requests")
data class VacancyApplicationRequest (/*@ManyToOne(fetch = FetchType.EAGER)
                                      @JoinColumn(name = "prawn_id", referencedColumnName = "user_id")
                                      var prawn: Prawn? = null*/

                                      @ManyToOne(fetch = FetchType.EAGER)
                                      @JoinColumn(name = "vacancy_id", referencedColumnName = "id")
                                      var vacancy: Vacancy? = null) {
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private val request: Request? = null
}