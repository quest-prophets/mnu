package mnu.model.request

import mnu.model.Client
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table (name = "new_vacancy_requests")
data class NewVacancyRequest (@Column(nullable = false) var title: String = "",
                              @Min(1) var salary: Long = 0,
                              @Min(0) var requiredKarma: Long = 0,
                              @Min(0) var workHoursPerWeek: Int = 0,
                              @Min(1) var vacantPlaces: Long? = null,

                              @ManyToOne(fetch = FetchType.EAGER)
                              @JoinColumn(name = "requester_id", referencedColumnName = "id")
                              var client: Client? = null) {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    var request: Request? = null


}